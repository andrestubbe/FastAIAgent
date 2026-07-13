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
 * Demo referencing Chapter 12: Ethical and Explainable Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Explainable Agent (Loan Approval with Transparent Reasoning & Confidence).
 */
public final class ExplainableAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Explainable Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Credit Score lookup only)
        FastAIRuntime bankRuntime = new FastAIRuntime();
        bankRuntime.register(new FastTool() {
            @Override public String name() { return "bank.get_credit_score"; }
            @Override public String description() { return "customer_id=<id>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String custId = String.valueOf(args.get("customer_id")).trim();
                System.out.println("Fetching credit score for customer " + custId + "...");
                String score = "720"; // Excellent score
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Credit Score: " + score; }
                };
            }
        });

        String bankSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(bankRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bankBot = new FastAIBot(brain, bankSystemPrompt, out, out);
        FastAIAgent bankAgent = new FastAIAgent(bankBot, bankRuntime, new TokyoNightLogger());

        // Step 1: Fetch Credit Score
        System.out.println("\n--- STEP 1: FETCHING CREDIT SCORE ---");
        bankAgent.run("Get credit score for customer CUST-101.");

        String scoreVal = bankBot.getHistory().messages().getLast().text();
        if (scoreVal.contains("Tool Execution Result (bank.get_credit_score): ")) {
            scoreVal = scoreVal.replace("Tool Execution Result (bank.get_credit_score): ", "").trim();
        }

        // 2. Step 2 Runtime (Save Explained Decision only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        String explainableSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nEXPLAINABILITY RULES:\n" +
                "- Decide loan approval (Threshold: score >= 700 is APPROVED, else REJECTED).\n" +
                "- Clearly state the final decision.\n" +
                "- Provide a step-by-step reasoning justification.\n" +
                "- Explicitly output your decision confidence level (e.g. Confidence: X%) and explain why.\n" +
                "You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, explainableSystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Run Step 2: Explainable Decision saving
        System.out.println("\n--- STEP 2: GENERATING EXPLAINABLE LOAN DECISION ---");
        saveAgent.run(String.format(
            "Based on the customer credit score of '%s', determine loan approval, write a detailed step-by-step explanation and your confidence level, and save the report to target/credit_decision.txt",
            scoreVal
        ));
    }
}
