package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import demo.tools.ChartGenerationTool;
import fastterminal.FastTerminal;

public final class ChartGenerationAgentDemo {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Chart Generation Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());
        runtime.register(new ChartGenerationTool());

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nFor generating a text chart from numerical data, use: chart.generate_ascii|data=<comma_separated_numbers>" +
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        agent.run("Generate an ASCII bar chart for the sales data '10, 45, 30, 80, 20'.");
        agent.run("Save the generated chart to 'target/sales_chart.txt' exactly as returned.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
