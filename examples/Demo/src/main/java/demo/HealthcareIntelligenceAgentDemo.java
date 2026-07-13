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
 * Feature: Healthcare Intelligence Agent (symptom evaluator with emergency warning triggers).
 */
public final class HealthcareIntelligenceAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Healthcare Intelligence Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Medical Guidelines lookup only)
        FastAIRuntime medRuntime = new FastAIRuntime();
        medRuntime.register(new FastTool() {
            @Override public String name() { return "med.get_guidelines"; }
            @Override public String description() { return "condition=<flu|migraine|chest_pain>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String cond = String.valueOf(args.get("condition")).trim().toLowerCase();
                System.out.println("Fetching medical guidelines for: " + cond);
                String guide = switch (cond) {
                    case "chest_pain" -> "RED FLAG: Threat to life (suspected cardiac event). Trigger immediate emergency room escalation. Do not delay.";
                    case "flu" -> "REST: Stay home, hydrate, take over-the-counter fever reducers.";
                    default -> "MONITOR: Consult a primary care physician if symptoms persist.";
                };
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return guide; }
                };
            }
        });

        String medSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(medRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot medBot = new FastAIBot(brain, medSystemPrompt, out, out);
        FastAIAgent medAgent = new FastAIAgent(medBot, medRuntime, new TokyoNightLogger());

        // Step 1: Fetch Guidelines
        System.out.println("\n--- STEP 1: RETRIEVING CLINICAL GUIDELINES ---");
        medAgent.run("Get clinical guidelines for chest_pain.");

        String guidelines = medBot.getHistory().messages().getLast().text();
        if (guidelines.contains("Tool Execution Result (med.get_guidelines): ")) {
            guidelines = guidelines.replace("Tool Execution Result (med.get_guidelines): ", "").trim();
        }

        // 2. Step 2 Runtime (Save Advice Report only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        String safetySystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nMEDICAL SAFETY RULES:\n" +
                "- If clinical guidelines contain RED FLAG or emergency signals, put them in a prominent WARNING box.\n" +
                "- Write clear, actionable safety instructions for the patient.\n" +
                "- You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, safetySystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Run Step 2: Safety Advice Generation
        System.out.println("\n--- STEP 2: GENERATING PATIENT MEDICAL ADVICE REPORT ---");
        saveAgent.run(String.format(
            "Based on the clinical guidelines: '%s', write a patient advice report highlighting any emergency warnings, and save it to target/medical_advice.txt",
            guidelines
        ));
    }
}
