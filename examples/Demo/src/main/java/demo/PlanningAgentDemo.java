package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;
import fastterminal.FastTerminal;

import java.util.function.Consumer;

/**
 * Demo referencing Chapter 5: Foundational Cognitive Architectures
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 */
public final class PlanningAgentDemo {

    public static void main(String[] args) {
        try {
            FastTerminal.setAnsiRawMode(true);
        } catch (Throwable ignored) {
        }
        System.out.println("=== Running Planning Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime);
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        agent.run("Start application notepad.exe");

        FastTerminal.setAnsiRawMode(false);
    }
}
