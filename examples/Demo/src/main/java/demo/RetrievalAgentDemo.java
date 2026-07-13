package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import fastairag.FastAIRag;
import fastairag.RagStore;
import fastairag.RagDocument;
import demo.tools.RagSearchTool;
import fastterminal.FastTerminal;
import java.util.Map;

public final class RetrievalAgentDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Retrieval Agent Demo ===");

        // 1. Setup RAG Store with a mock embedding provider
        RagStore ragStore = FastAIRag.store(text -> {
            float[] vec = new float[128];
            vec[0] = Math.abs(text.hashCode()) % 100 / 100f;
            return vec;
        });

        // Add knowledge documents
        ragStore.add(new RagDocument("doc1", "The company vacation policy states that all employees get 30 days of paid leave per year.", Map.of("source", "HR_Policy.txt")));
        ragStore.add(new RagDocument("doc2", "To configure the VPN, connect to vpn.company.com using the standard credentials.", Map.of("source", "IT_Manual.txt")));
        ragStore.add(new RagDocument("doc3", "Our core values are: Innovation, Integrity, and Fast Execution.", Map.of("source", "Culture.md")));

        // 2. Setup Runtime
        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());
        runtime.register(new RagSearchTool(ragStore));

        // 3. Setup Agent
        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nFor searching the knowledge base, use: knowledge.search|query=<search_term>" +
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        // 4. Execute Tasks
        agent.run("Search the knowledge base for 'vacation policy'.");
        agent.run("Save a summary of the vacation policy to 'target/retrieval_output.txt'.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
