package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileReadTool;
import fastairuntime.tools.FileSaveTool;

import java.util.function.Consumer;

/**
 * Demo referencing Chapter 8: Data Analysis and Verification Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: General Problem Solver Agent that decomposes complex goals into sub-tasks.
 */
public final class GeneralProblemSolverDemo {

    public static void main(String[] args) {
        System.out.println("=== Running General Problem Solver Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        String fileToRead = "target/verification_output.txt";
        String fileToSave = "target/solved_output.txt";

        // 1. Step 1: Read the file content
        System.out.println("\n--- STEP 1: READ THE FILE ---");
        agent.run("Read the content of file " + fileToRead);

        // Extract file content from the tool observation output stored in history
        String lastMsg = bot.getHistory().messages().getLast().text();
        String content = "JAVA"; // Default fallback if parsing fails
        if (lastMsg.contains("File content:\n")) {
            content = lastMsg.substring(lastMsg.indexOf("File content:\n") + 14).trim();
        }

        // 2. Step 2: Save the backup copy using the read content
        System.out.println("\n--- STEP 2: SAVE THE BACKUP ---");
        agent.run(String.format("file.save|path=%s,content=%s", fileToSave, content));
    }
}
