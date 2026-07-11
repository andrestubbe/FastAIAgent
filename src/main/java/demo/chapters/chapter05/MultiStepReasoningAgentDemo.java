package demo.chapters.chapter05;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;

/**
 * Demo referencing Chapter 5: Foundational Cognitive Architectures
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: The Multi-Step Reasoning Agent (reasoning over complex compound goals).
 */
public final class MultiStepReasoningAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Multi-Step Reasoning Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());
        runtime.register(new FileSaveTool());

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        ConversationHistory memory = new ConversationHistory();
        FastAIAgent agent = new FastAIAgent(brain, runtime, memory);

        // Execute complex compound workflow
        agent.run("Create a local text file at target/reasoning_output.txt with content 'Executed multi-step logic successfully.'");
    }
}
