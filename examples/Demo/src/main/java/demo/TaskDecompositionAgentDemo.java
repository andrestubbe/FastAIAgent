package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 9: Software Development Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Task-Decomposition and Task-Execution Agent for Software Engineering.
 */
public final class TaskDecompositionAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Task-Decomposition & Task-Execution Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Planning Phase (Decomposition)
        System.out.println("\n--- PLANNING PHASE (DECOMPOSITION) ---");
        String request = "Create a Java utility class target/MathUtils.java with a method to calculate factorials.";
        System.out.println("Request: " + request);

        String planningPrompt = "Decompose this Java development request into exactly two tasks:\n" +
                "1. Task 1 to write the utility class source code.\n" +
                "2. Task 2 to write a simple test or verification log text.\n" +
                "Format each task line precisely like: TASK: <target_file> | <instructions_for_code_content>\n" +
                "No other text, explanations, or thoughts.";

        String plan = brain.ask("You are a Lead Software Architect.", planningPrompt).trim();
        System.out.println("Generated Tasks Plan:\n" + plan);

        // Parse tasks
        List<DevTask> tasks = new ArrayList<>();
        for (String line : plan.split("\n")) {
            if (line.trim().startsWith("TASK:")) {
                String raw = line.substring(5).trim();
                String[] parts = raw.split("\\|", 2);
                if (parts.length == 2) {
                    tasks.add(new DevTask(parts[0].trim(), parts[1].trim()));
                }
            }
        }

        // 2. Execution Phase (Developer Agent)
        System.out.println("\n--- EXECUTION PHASE (CODE GENERATION) ---");
        FastAIRuntime devRuntime = new FastAIRuntime();
        devRuntime.register(new FileSaveTool());

        String devSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(devRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot devBot = new FastAIBot(brain, devSystemPrompt, out, out);
        FastAIAgent devAgent = new FastAIAgent(devBot, devRuntime, new TokyoNightLogger());

        for (int i = 0; i < tasks.size(); i++) {
            DevTask task = tasks.get(i);
            System.out.println(String.format("\n[Task %d/%d] Writing file: %s", i + 1, tasks.size(), task.file));
            
            // Instruct developer agent to write the content
            String devInstruction = String.format(
                "Save a file named %s containing the source code or log for: %s",
                task.file, task.instructions
            );
            devAgent.run(devInstruction);
        }

        System.out.println("\n✅ All decomposed tasks executed successfully!");
    }

    private record DevTask(String file, String instructions) {}
}
