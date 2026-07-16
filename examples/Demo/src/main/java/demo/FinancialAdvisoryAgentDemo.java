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
 * Demo referencing Chapter 14: Financial and Legal Domain Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Financial Advisory Agent (portfolio valuation & compliance advice).
 */
public final class FinancialAdvisoryAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Financial Advisory Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Market data lookup only)
        FastAIRuntime marketRuntime = new FastAIRuntime();
        marketRuntime.register(new FastTool() {
            @Override public String name() { return "finance.get_stock_price"; }
            @Override public String description() { return "ticker=<ticker>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String ticker = String.valueOf(args.get("ticker")).trim().toUpperCase();
                System.out.println("Fetching current market price for ticker: " + ticker);
                String price = "150"; // $150
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Price: $" + price; }
                };
            }
        });

        String marketSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(marketRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot marketBot = new FastAIBot(brain, marketSystemPrompt, out, out);
        FastAIAgent marketAgent = new FastAIAgent(marketBot, marketRuntime, new TokyoNightLogger());

        // Step 1: Fetch Price
        System.out.println("\n--- STEP 1: RETRIEVING MARKET STOCK PRICE ---");
        marketAgent.run("Get stock price for AAPL.");

        String priceVal = marketBot.getHistory().messages().getLast().text();
        if (priceVal.contains("Tool Execution Result (finance.get_stock_price): ")) {
            priceVal = priceVal.replace("Tool Execution Result (finance.get_stock_price): ", "").trim();
        }

        // 2. Step 2 Runtime (Save Advice Report only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        String financeSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nFINANCIAL COMPLIANCE RULES:\n" +
                "- If the stock price is below $160, issue a BUY recommendation.\n" +
                "- State the current price and valuation relative to the $160 threshold.\n" +
                "- Include a mandatory regulatory disclaimer: 'Disclaimer: This is not professional financial advice.'\n" +
                "You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, financeSystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Run Step 2: Financial Recommendation Generation
        System.out.println("\n--- STEP 2: GENERATING FINANCIAL ADVICE REPORT ---");
        saveAgent.run(String.format(
            "Based on the stock price: '%s', determine the investment action for AAPL, include a mandatory disclaimer, and save the report to target/financial_advice.txt",
            priceVal
        ));
    }
}
