package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;
import fastterminal.FastTerminal;

import java.util.function.Consumer;

/**
 * Demo referencing Chapter 5: Foundational Cognitive Architectures
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: The Multi-Step Reasoning Agent (reasoning over complex compound goals).
 */
public final class MultiStepReasoningAgentDemo {

    public static void main(String[] args) {
        try { fastterminal.FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Multi-Step Reasoning Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());
        runtime.register(new FileSaveTool());

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime);
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        // Execute complex compound workflow
        agent.run("Create a local text file at target/reasoning_output.txt with content 'Executed multi-step logic successfully.'");
        
        FastTerminal.setAnsiRawMode(false);
    }
}
