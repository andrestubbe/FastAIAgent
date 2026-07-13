package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastairuntime.tools.FileReadTool;
import fastairuntime.tools.FileSaveTool;
import fastterminal.FastTerminal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 7: Tool Manipulation and Orchestration Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Chain-of-Agents Coordinator orchestrating Researcher and Writer agents.
 */
public final class MultiAgentOrchestratorDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Multi-Agent Coordinator Demo ===");

        System.out.println("Connecting directly to local Ollama...");
        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> System.out.print(token);

        // 1. Setup Specialist Runtime & Bot for the Researcher
        FastAIRuntime researcherRuntime = new FastAIRuntime();
        researcherRuntime.register(new FileReadTool());
        // In a real scenario, this would have web search/browser tools registered
        String researcherSystemPrompt = fastaiagent.FastAIPromptBuilder.buildSystemPrompt(researcherRuntime) + 
                "\nYou are a Researcher Agent. Answer queries factually and concisely using any available tools.";
        FastAIBot researcherBot = new FastAIBot(brain, researcherSystemPrompt, out, token -> {});
        FastAIAgent researcherAgent = new FastAIAgent(researcherBot, researcherRuntime, new TokyoNightLogger());

        // 2. Setup Specialist Runtime & Bot for the Writer
        FastAIRuntime writerRuntime = new FastAIRuntime();
        writerRuntime.register(new FileSaveTool());
        String writerSystemPrompt = fastaiagent.FastAIPromptBuilder.buildSystemPrompt(writerRuntime) + 
                "\nYou are a Writer Agent. Format notes into a beautiful, structured markdown report and save it to the specified target path using the 'file.save' tool.";
        FastAIBot writerBot = new FastAIBot(brain, writerSystemPrompt, out, token -> {});
        FastAIAgent writerAgent = new FastAIAgent(writerBot, writerRuntime, new TokyoNightLogger());

        // 3. Setup Coordinator Runtime and Bot
        FastAIRuntime coordinatorRuntime = new FastAIRuntime();

        // Register Delegation Tool for Researcher
        coordinatorRuntime.register(new FastTool() {
            @Override
            public String name() { return "delegate.research"; }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                String query = String.valueOf(args.get("query"));
                System.out.println("\n[Coordinator] Delegating Research: \"" + query + "\"");
                researcherAgent.run(query);
                String lastResponse = researcherBot.getHistory().messages().getLast().text();
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Researcher result: " + lastResponse; }
                };
            }
        });

        // Register Delegation Tool for Writer
        coordinatorRuntime.register(new FastTool() {
            @Override
            public String name() { return "delegate.writer"; }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                String notes = String.valueOf(args.get("notes"));
                String targetFile = String.valueOf(args.get("target_file"));
                System.out.println("\n[Coordinator] Delegating Write to: " + targetFile);
                writerAgent.run("Format these notes and save to " + targetFile + ". Notes: " + notes);
                String lastResponse = writerBot.getHistory().messages().getLast().text();
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Writer result: " + lastResponse; }
                };
            }
        });

        String coordinatorSystemPrompt = "You are the Coordinator Agent. Your goal is to coordinate a Researcher and a Writer to compile a research report.\n" +
                "Available tools:\n" +
                "- delegate.research: query=<research_topic>\n" +
                "- delegate.writer: notes=<notes_to_write>,target_file=<file_path>\n" +
                "Output format: tool_name|arg_key=arg_value. Always reply with a single structured tool call, no extra text.";

        FastAIBot coordinatorBot = new FastAIBot(brain, coordinatorSystemPrompt, out, token -> {});
        FastAIAgent coordinatorAgent = new FastAIAgent(coordinatorBot, coordinatorRuntime, new TokyoNightLogger());

        // 4. Orchestration Goal
        String goal = "Research the main features of Java 17, summarize them, and save the report to target/java17_report.txt";
        
        System.out.println("\nGoal: " + goal);

        // Turn 1: Delegate research
        System.out.println("\n--- COORDINATOR STEP 1 ---");
        coordinatorAgent.run("Start by researching Java 17 main features.");

        // Turn 2: Delegate write using the research findings
        System.out.println("\n--- COORDINATOR STEP 2 ---");
        coordinatorAgent.run("Based on the research findings, draft and save the report to target/java17_report.txt");

    }
}
