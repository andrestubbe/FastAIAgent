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
 * Demo referencing Chapter 7: Tool Manipulation and Orchestration Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Stateful Agentic Workflow (State-Machine) using FastAIAgent Tokyo Night UI.
 */
public final class AgenticWorkflowDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Stateful Agentic Workflow Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());

        // Register custom Inventory Tool
        runtime.register(new FastTool() {
            @Override
            public String name() { return "inventory.check"; }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                int amount = Integer.parseInt(String.valueOf(args.get("amount")));
                boolean available = amount <= 10;
                return new FastObservation() {
                    @Override public boolean success() { return available; }
                    @Override public String message() { return available ? "Inventory: AVAILABLE" : "Inventory: OUT OF STOCK"; }
                };
            }
        });

        // Register custom Fraud Tool
        runtime.register(new FastTool() {
            @Override
            public String name() { return "fraud.check"; }

            @Override
            public FastObservation execute(Map<String, Object> args) {
                String customer = String.valueOf(args.get("customer"));
                double total = 0.0;
                if (args.containsKey("total")) {
                    total = Double.parseDouble(String.valueOf(args.get("total")));
                } else if (args.containsKey("total_cost")) {
                    total = Double.parseDouble(String.valueOf(args.get("total_cost")));
                }
                String prompt = String.format("Analyze order for %s, total $%.2f. Reply SAFE or SUSPICIOUS.", customer, total);
                String evaluation = brain.ask("You are a fraud risk analyst.", prompt).trim();
                boolean safe = evaluation.toUpperCase().contains("SAFE");
                return new FastObservation() {
                    @Override public boolean success() { return safe; }
                    @Override public String message() { return "Fraud Check: " + evaluation; }
                };
            }
        });

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        // Step 1: Inventory Check
        System.out.println("\n--- STEP 1: INVENTORY CHECK ---");
        agent.run("Check inventory for Premium AI Laptop with amount 5.");

        // Step 2: Fraud Assessment
        System.out.println("\n--- STEP 2: FRAUD CHECK ---");
        agent.run("Run fraud assessment for customer John Doe with total cost 7500.00.");

        // Step 3: Write report
        System.out.println("\n--- STEP 3: WRITE REPORT ---");
        agent.run("Save order log to target/workflow_output.txt with content 'Order approved and processed successfully.'");
    }
}
