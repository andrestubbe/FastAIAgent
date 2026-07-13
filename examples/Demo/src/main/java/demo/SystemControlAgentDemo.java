package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.CommandRunnerTool;
import fastairuntime.tools.WindowsAppTool;
import fastairuntime.tools.WindowsCloseAppTool;
import fastterminal.FastTerminal;

public final class SystemControlAgentDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running System Control Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new CommandRunnerTool());
        runtime.register(new WindowsAppTool());
        runtime.register(new WindowsCloseAppTool());

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        agent.run("Run the OS command 'ping 127.0.0.1 -n 2'.");
        agent.run("Open the Windows Calculator application (calc.exe).");
        agent.run("Close the Windows Calculator application (CalculatorApp.exe).");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
