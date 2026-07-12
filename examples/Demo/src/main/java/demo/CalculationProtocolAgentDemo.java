package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;
import fastairuntime.tools.WindowsCloseAppTool;
import fastterminal.FastTerminal;

import java.util.function.Consumer;

/**
 * Demo referencing Chapter 5: Foundational Cognitive Architectures
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: The Calculation Protocol Agent (opens calc, performs operation, logs result).
 */
public final class CalculationProtocolAgentDemo {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try {
            FastTerminal.setAnsiRawMode(true);
        } catch (Throwable ignored) {
        }
        System.out.println("=== Running Calculation Protocol Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new WindowsCloseAppTool());
        runtime.register(new KeyboardTypeTool());
        runtime.register(new FileSaveTool());

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};
        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) +
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime);

        // Step 1: Open Calculator
        agent.run("Open the Windows Calculator application located at calc.exe");

        // Step 2: Type the calculation
        agent.run("Type the text '42*7=' using the keyboard");

        // Step 3: Log the result to a file
        agent.run("Save a calculation protocol to target/calculation_log.txt with content 'Calculation: 42 * 7 = 294'");

        // Step 4: Close the calculator
        agent.run("Close the Windows Calculator application (CalculatorApp.exe)");

        long duration = System.currentTimeMillis() - start;
        System.out.println("\n⏱️ Total Execution Time: " + duration + " ms\n");

        FastTerminal.setAnsiRawMode(false);
    }
}
