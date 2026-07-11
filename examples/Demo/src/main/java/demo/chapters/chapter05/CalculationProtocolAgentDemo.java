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
 * Feature: The Calculation Protocol Agent (opens calc, performs operation, logs result).
 */
public final class CalculationProtocolAgentDemo {

    public static void main(String[] args) {
        try { fastterminal.FastTerminal.setAnsiRawMode(true); } catch (Throwable ignored) {}
        System.out.println("=== Running Calculation Protocol Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());
        runtime.register(new FileSaveTool());

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        ConversationHistory memory = new ConversationHistory();
        FastAIAgent agent = new FastAIAgent(brain, runtime, memory);

        // Step 1: Open Calculator
        agent.run("Open the Windows Calculator application located at calc.exe");

        // Step 2: Type the calculation
        agent.run("Type the text '42*7=' using the keyboard");

        // Step 3: Log the result to a file
        agent.run("Save a calculation protocol to target/calculation_log.txt with content 'Calculation: 42 * 7 = 294'");
    }
}
