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

public final class DocumentQAAgentDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Document QA Agent Demo ===");

        RagStore ragStore = FastAIRag.store(text -> {
            float[] vec = new float[128];
            vec[0] = Math.abs(text.hashCode()) % 100 / 100f;
            return vec;
        });

        // Add knowledge documents
        ragStore.add(new RagDocument("doc1", "Employee 'Alice Smith' works in the Engineering department.", Map.of("source", "Directory.csv")));
        ragStore.add(new RagDocument("doc2", "The Engineering department manager is 'Bob Jones'.", Map.of("source", "OrgChart.txt")));

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());
        runtime.register(new RagSearchTool(ragStore));

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nFor searching the knowledge base, use: knowledge.search|query=<search_term>" +
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        agent.run("Search the knowledge base to find out which department Alice Smith works in.");
        agent.run("Search the knowledge base again to find out who manages the Engineering department.");
        agent.run("Save a file 'target/qa_answer.txt' answering the question: 'Who is Alice Smith's manager?'.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
