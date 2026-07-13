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

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());
        
        // Register Catalog Tool
        runtime.register(new FastTool() {
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

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        String userInterests = "distributed systems, databases";
        System.out.println("\nUser Interests: " + userInterests);

        // Step 1: Fetch Catalog
        System.out.println("\n--- STEP 1: FETCHING CATALOG ---");
        agent.run("Fetch the book catalog.");

        // Step 2: Personalize & Save Recommendations
        System.out.println("\n--- STEP 2: PERSONALIZING & SAVING RECOMMENDATIONS ---");
        agent.run(String.format("Recommend the best book matching interests '%s' and save it to target/recommendations.txt", userInterests));
    }
}
