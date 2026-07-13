package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.BrowserTool;
import fastairuntime.tools.FileSaveTool;
import fastterminal.FastTerminal;

public final class BrowserAutomationAgentDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Browser Automation Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new BrowserTool());
        runtime.register(new FileSaveTool());

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        agent.run("Fetch the content of 'https://example.com'.");
        agent.run("Save a summary indicating that the website was successfully fetched to 'target/browser_log.txt'.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
