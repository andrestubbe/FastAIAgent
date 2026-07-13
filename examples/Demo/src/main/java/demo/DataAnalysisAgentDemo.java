package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import fastairuntime.tools.FileReadTool;
import demo.tools.DataAnalysisTool;
import fastterminal.FastTerminal;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DataAnalysisAgentDemo {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Data Analysis Agent Demo ===");

        // Mock data file for the agent to read
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/sales_q1.txt"), "1500.50, 2300.75, 1800.00, 3400.20, 2900.10");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());
        runtime.register(new DataAnalysisTool());

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nFor analyzing comma-separated numerical data, use: data.analyze|data=<comma_separated_numbers>" +
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        agent.run("Read the contents of 'target/sales_q1.txt'.");
        agent.run("Analyze the numerical data '1500.50, 2300.75, 1800.00, 3400.20, 2900.10' using the data analysis tool.");
        agent.run("Save a brief report with the mean and max values to 'target/sales_report.txt'.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
