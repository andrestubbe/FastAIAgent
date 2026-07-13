package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastairuntime.tools.CommandRunnerTool;
import fastairuntime.tools.FileSaveTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 7: Tool Manipulation and Orchestration Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Interactive CLI Agent with Human-in-the-Loop Approval Checkpoint.
 */
public final class InteractiveAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Interactive CLI Agent with Human-in-the-Loop ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        FastAIRuntime runtime = new FastAIRuntime();

        // 1. Register Proxy FileSaveTool
        FastTool realFileSave = new FileSaveTool();
        runtime.register(new FastTool() {
            @Override
            public String name() { return realFileSave.name(); }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                if (askUserApproval("Save file to " + args.get("path"))) {
                    return realFileSave.execute(args);
                } else {
                    return new FastObservation() {
                        @Override public boolean success() { return false; }
                        @Override public String message() { return "Action rejected by human."; }
                    };
                }
            }
        });

        // 2. Register Proxy CommandRunnerTool
        FastTool realCommandRunner = new CommandRunnerTool();
        runtime.register(new FastTool() {
            @Override
            public String name() { return realCommandRunner.name(); }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                if (askUserApproval("Run OS command: " + args.get("command"))) {
                    return realCommandRunner.execute(args);
                } else {
                    return new FastObservation() {
                        @Override public boolean success() { return false; }
                        @Override public String message() { return "Action rejected by human."; }
                    };
                }
            }
        });

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";

        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            System.out.println("\nEnter your goals (type 'exit' to quit).");
            
            while (true) {
                System.out.print("\nGoal Input > ");
                String goal = reader.readLine();
                if (goal == null || goal.trim().equalsIgnoreCase("exit")) {
                    break;
                }
                if (goal.trim().isEmpty()) continue;

                // Run agent turn on the user's goal
                agent.run(goal);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean askUserApproval(String action) {
        System.out.print(String.format("\n⚠️  [HUMAN APPROVAL REQUIRED] Action: %s\nApprove? (yes/no): ", action));
        System.out.flush();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String answer = inputReader.readLine();
            return answer != null && (answer.trim().equalsIgnoreCase("yes") || answer.trim().equalsIgnoreCase("y"));
        } catch (Exception e) {
            return false;
        }
    }
}
