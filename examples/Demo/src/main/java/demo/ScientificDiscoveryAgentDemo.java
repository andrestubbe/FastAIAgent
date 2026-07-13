package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastairuntime.tools.FileSaveTool;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 13: Healthcare and Scientific Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Scientific Discovery Agent (literature synthesis & hypothesis generator).
 */
public final class ScientificDiscoveryAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Scientific Discovery Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Literature Search only)
        FastAIRuntime scienceRuntime = new FastAIRuntime();
        scienceRuntime.register(new FastTool() {
            @Override public String name() { return "science.search_papers"; }
            @Override public String description() { return "query=<query>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String query = String.valueOf(args.get("query")).trim().toLowerCase();
                System.out.println("Searching papers for query: " + query);
                String papers = "Literature Findings:\n" +
                        "- Paper A: Observed near room-temperature superconductivity in LK-99 under high pressure.\n" +
                        "- Paper B: Failed to replicate zero resistance at ambient pressure.\n" +
                        "- Paper C: Identified structural instability as the primary replication bottleneck.";
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return papers; }
                };
            }
        });

        String scienceSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(scienceRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot scienceBot = new FastAIBot(brain, scienceSystemPrompt, out, out);
        FastAIAgent scienceAgent = new FastAIAgent(scienceBot, scienceRuntime, new TokyoNightLogger());

        // Step 1: Search Literature
        System.out.println("\n--- STEP 1: SEARCHING SCIENTIFIC LITERATURE ---");
        scienceAgent.run("Search literature for room_temperature_superconductivity.");

        String literature = scienceBot.getHistory().messages().getLast().text();
        if (literature.contains("Tool Execution Result (science.search_papers): ")) {
            literature = literature.replace("Tool Execution Result (science.search_papers): ", "").trim();
        }

        // 2. Step 2 Runtime (Save Hypothesis Report only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        String researchSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nRESEARCH RULES:\n" +
                "- Analyze the findings to identify the main knowledge gap (e.g. ambient pressure replication).\n" +
                "- Formulate a testable abductive research hypothesis.\n" +
                "- You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, researchSystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Run Step 2: Scientific Synthesis and Hypothesis generation
        System.out.println("\n--- STEP 2: SYNTHESIZING KNOWLEDGE GAP & SAVING HYPOTHESIS ---");
        saveAgent.run(String.format(
            "Based on findings: '%s', synthesize the primary knowledge gap, formulate a testable hypothesis, and save the report to target/scientific_hypothesis.txt",
            literature
        ));
    }
}
