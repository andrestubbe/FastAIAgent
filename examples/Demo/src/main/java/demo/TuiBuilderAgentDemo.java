package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastairuntime.FastAIRuntime;
import demo.tools.tui.*;
import fastterminal.FastTerminal;

public final class TuiBuilderAgentDemo {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        try { FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running TUI Builder Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        FastTUIContext tuiContext = new FastTUIContext(80, 24);
        
        runtime.register(new TUIClearTool(tuiContext));
        runtime.register(new TUISetColorTool(tuiContext));
        runtime.register(new TUIDrawBoxTool(tuiContext));
        runtime.register(new TUIDrawTextTool(tuiContext));
        runtime.register(new TUIDrawPanelTool(tuiContext));

        AI brain = FastAI.connect("ollama:qwen2.5-coder:1.5b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
            "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        agent.run("Clear the TUI screen and set the color to Tokyo Night foreground (0xc0caf5) and background (0x1a1b26).");
        agent.run("Draw a panel with title 'System Info' and content 'Status: ONLINE\nAgent: Active\nCPU: 4%'");
        agent.run("Draw another panel with title 'Thoughts' and content 'Initializing visual engine...'");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");
        FastTerminal.setAnsiRawMode(false);
    }
}
