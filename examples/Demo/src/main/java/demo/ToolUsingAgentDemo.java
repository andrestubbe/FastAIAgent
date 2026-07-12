package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.*;
import fastterminal.FastTerminal;

public final class ToolUsingAgentDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Tool-Using Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        // Register a massive toolset to test tool selection
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());
        runtime.register(new DirectoryListTool());
        runtime.register(new CommandRunnerTool());
        runtime.register(new BrowserTool());
        runtime.register(new KeyboardTypeTool());
        runtime.register(new WindowsAppTool());
        runtime.register(new WindowsCloseAppTool());

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        agent.run("Run the OS command 'echo Hello Tool World'.");
        agent.run("Save a file 'target/tool_selection.txt' with the content 'Selected the correct tool'.");
        agent.run("List the directory contents of 'target'.");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
