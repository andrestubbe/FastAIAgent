package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import demo.tools.DataAnalysisTool;
import fastterminal.FastTerminal;

public final class StatisticalAgentDemo {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Statistical Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());
        runtime.register(new DataAnalysisTool());

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nFor analyzing comma-separated numerical data, use: data.analyze|data=<comma_separated_numbers>" +
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        agent.run("Analyze the performance metrics '1.2, 1.4, 1.1, 1.3, 1.25' using the data analysis tool.");
        agent.run("Analyze the secondary performance metrics '0.8, 1.9, 0.5, 2.1, 1.0' using the data analysis tool.");
        agent.run("Based on the standard deviations, save a file 'target/stats_conclusion.txt' stating which dataset is more stable.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
