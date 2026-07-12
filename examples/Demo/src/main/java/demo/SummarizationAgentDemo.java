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

public final class SummarizationAgentDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Summarization Agent Demo ===");

        RagStore ragStore = FastAIRag.store(text -> {
            float[] vec = new float[128];
            vec[0] = Math.abs(text.hashCode()) % 100 / 100f;
            return vec;
        });

        // Add knowledge documents
        ragStore.add(new RagDocument("doc1", "Project Alpha Phase 1: We successfully launched the new UI in Q1.", Map.of("source", "Project_Alpha.md")));
        ragStore.add(new RagDocument("doc2", "Project Alpha Phase 2: Backend migration completed in Q2 with 15% latency reduction.", Map.of("source", "Project_Alpha.md")));
        ragStore.add(new RagDocument("doc3", "Project Alpha Phase 3: Mobile app rollout scheduled for Q3.", Map.of("source", "Project_Alpha.md")));

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());
        runtime.register(new RagSearchTool(ragStore));

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nFor searching the knowledge base, use: knowledge.search|query=<search_term>" +
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        agent.run("Search the knowledge base for all updates regarding 'Project Alpha'.");
        agent.run("Save a brief summary of all three phases of Project Alpha to 'target/project_alpha_summary.txt'.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
