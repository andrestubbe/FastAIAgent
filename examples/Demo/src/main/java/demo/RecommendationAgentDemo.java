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
 * Demo referencing Chapter 10: Conversational and Content Creation Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Recommendation Agent (personalized book recommendation delivery).
 */
public final class RecommendationAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Recommendation Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Catalog only)
        FastAIRuntime catalogRuntime = new FastAIRuntime();
        catalogRuntime.register(new FastTool() {
            @Override public String name() { return "recommend.get_catalog"; }
            @Override public String description() { return "no arguments"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String catalog = "Catalog:\n" +
                        "1. Designing Data-Intensive Applications (Topics: distributed systems, databases, architecture)\n" +
                        "2. Hands-On Machine Learning (Topics: machine learning, python, neural networks)\n" +
                        "3. Clean Code (Topics: software design, java, refactoring)";
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return catalog; }
                };
            }
        });

        String catalogSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(catalogRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot catalogBot = new FastAIBot(brain, catalogSystemPrompt, out, out);
        FastAIAgent catalogAgent = new FastAIAgent(catalogBot, catalogRuntime, new TokyoNightLogger());

        String userInterests = "distributed systems, databases";
        System.out.println("\nUser Interests: " + userInterests);

        // Step 1: Fetch Catalog
        System.out.println("\n--- STEP 1: FETCHING CATALOG ---");
        catalogAgent.run("Fetch the book catalog.");

        // Extract catalog content from history
        String lastMsg = catalogBot.getHistory().messages().getLast().text();
        String catalogContent = "";
        if (lastMsg.contains("Catalog:\n")) {
            catalogContent = lastMsg.substring(lastMsg.indexOf("Catalog:\n")).trim();
        }

        // 2. Step 2 Runtime (File saving only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        String saveSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, saveSystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Step 2: Personalize & Save Recommendations
        System.out.println("\n--- STEP 2: PERSONALIZING & SAVING RECOMMENDATIONS ---");
        saveAgent.run(String.format(
            "file.save|path=target/recommendations.txt,content=Recommend the best book matching '%s' from catalog:\n%s",
            userInterests, catalogContent
        ));
    }
}
