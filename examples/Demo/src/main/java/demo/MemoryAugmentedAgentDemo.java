package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;
import fastterminal.FastTerminal;

/**
 * Demo referencing Chapter 5: Foundational Cognitive Architectures
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: The Memory-Augmented Agent maintaining context across multiple steps.
 */
public final class MemoryAugmentedAgentDemo {

    public static void main(String[] args) {
        try { fastterminal.FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Memory-Augmented Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        java.util.function.Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime);
        fastaibot.FastAIBot bot = new fastaibot.FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        // Sequence Step 1
        System.out.println("\n--- STEP 1 ---");
        agent.run("Start application notepad.exe");

        // Sequence Step 2 (requires memory of the open app context)
        System.out.println("\n--- STEP 2 ---");
        agent.run("Type 'Hello from Memory-Augmented Agent!' inside the open window.");

        FastTerminal.setAnsiRawMode(false);
    }
}
