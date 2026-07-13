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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 8: Data Analysis and Verification Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Verification & Validation Agent for Fact-Checking and Constraint Verification.
 */
public final class VerificationAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Verification & Validation Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Setup Verifier Agent Runtime and custom Validation Tool
        FastAIRuntime verifierRuntime = new FastAIRuntime();
        verifierRuntime.register(new FastTool() {
            @Override
            public String name() { return "validation.check"; }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                String draft = String.valueOf(args.get("draft")).trim();
                boolean valid = draft.equals("JAVA");
                return new FastObservation() {
                    @Override public boolean success() { return valid; }
                    @Override public String message() { return valid ? "VALID" : "INVALID: The draft is not exactly the word 'JAVA' in uppercase."; }
                };
            }
        });

        String verifierSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(verifierRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot verifierBot = new FastAIBot(brain, verifierSystemPrompt, out, out);
        FastAIAgent verifierAgent = new FastAIAgent(verifierBot, verifierRuntime, new TokyoNightLogger());

        // 2. Setup Generator Agent Runtime (empty initially, so it writes plain text)
        FastAIRuntime generatorRuntime = new FastAIRuntime();
        String generatorSystemPrompt = "You are a Technical Writer. Output the exact word requested by the user. Write plain text, do NOT output any tool calls.";
        FastAIBot generatorBot = new FastAIBot(brain, generatorSystemPrompt, out, out);
        FastAIAgent generatorAgent = new FastAIAgent(generatorBot, generatorRuntime, new TokyoNightLogger());

        // 3. Execution Loop
        String request = "Write the word 'java' in uppercase. Constraints: 1. Must be exactly 'JAVA', 2. Must be uppercase.";
        String draft = "";
        boolean isApproved = false;

        System.out.println("\nInitial Request: " + request);

        // Run up to 3 revision cycles
        for (int cycle = 1; cycle <= 3; cycle++) {
            System.out.println(String.format("\n--- GENERATOR CYCLE %d ---", cycle));
            String prompt = request;
            if (!draft.isEmpty()) {
                prompt = "Correct your previous response to match the constraints. Previous response: " + draft;
            }
            generatorAgent.run(prompt);
            draft = generatorBot.getHistory().messages().getLast().text().trim();

            System.out.println(String.format("\n--- VERIFIER CYCLE %d ---", cycle));
            verifierAgent.run("validation.check|draft=" + draft.replace("|", "\\|").replace("\n", " "));
            
            String verificationResult = verifierBot.getHistory().messages().getLast().text();
            if (verificationResult.toUpperCase().contains("VALID") && !verificationResult.toUpperCase().contains("INVALID")) {
                isApproved = true;
                System.out.println("\n✅ Verification SUCCESS! Draft is approved.");
                break;
            } else {
                System.out.println("\n❌ Verification FAILED. Feedback received: " + verificationResult);
                request = "The previous response was rejected. You must output exactly the word 'JAVA' in uppercase: " + verificationResult;
            }
        }

        if (isApproved) {
            System.out.println("\nSaving final approved report via Agent Tool...");
            // Dynamically register FileSaveTool and rebuild system prompt for tool calling
            generatorRuntime.register(new FileSaveTool());
            String saveSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(generatorRuntime) + 
                    "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
            FastAIBot saveBot = new FastAIBot(brain, saveSystemPrompt, out, out);
            FastAIAgent saveAgent = new FastAIAgent(saveBot, generatorRuntime, new TokyoNightLogger());

            saveAgent.run("file.save|path=target/verification_output.txt,content=" + draft.replace("|", "\\|").replace("\n", " "));
        } else {
            System.out.println("\n⚠️ Could not get an approved version within the cycle limit.");
        }
    }
}
