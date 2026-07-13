package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.FileSaveTool;

import java.util.function.Consumer;

/**
 * Demo referencing Chapter 10: Conversational and Content Creation Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Content Creation Agent (generates structured markdown blogs).
 */
public final class ContentCreatorAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Content Creation Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        // Raw input notes
        String notes = "- AI Agents are cognitive minds with planning loops.\n" +
                       "- They use tool registries like FastAIRuntime to execute commands.\n" +
                       "- They coordinate using multi-agent architectures.";
        
        System.out.println("\nInput Notes:\n" + notes);

        // Instruct agent to draft a blog post and save it
        String instruction = "Draft a short, structured markdown blog post based on these notes and save it to target/generated_blog.md:\n" + notes;
        
        System.out.println("\nDrafting and saving blog post...");
        agent.run(instruction);
    }
}
