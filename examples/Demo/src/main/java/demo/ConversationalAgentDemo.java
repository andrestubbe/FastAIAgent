package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 10: Conversational and Content Creation Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: MindBridge - Campus Wellness Conversational Chatbot.
 */
public final class ConversationalAgentDemo {

    private static final Path LOG_PATH = Path.of("target/wellness_chat.txt");
    private static final StringBuilder CHAT_LOG = new StringBuilder();

    public static void main(String[] args) {
        System.out.println("=== Running MindBridge Wellness Conversational Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // Setup Runtime
        FastAIRuntime runtime = new FastAIRuntime();

        // 1. Recommend Activity Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "wellness.recommend_activity"; }
            @Override public String description() { return "mood=<stressed|tired|anxious|unmotivated>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String mood = String.valueOf(args.get("mood")).trim().toLowerCase();
                String rec = switch (mood) {
                    case "stressed" -> "Try a 5-minute breathing exercise in the campus garden.";
                    case "tired" -> "Take a 20-minute power nap or try a quick stretch routine.";
                    case "anxious" -> "Try our guided sensory grounding activity (5-4-3-2-1 technique).";
                    default -> "Go for a short 10-minute walk outside around the library.";
                };
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Recommendation: " + rec; }
                };
            }
        });

        // 2. Get Resource Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "wellness.get_resource"; }
            @Override public String description() { return "topic=<sleep|exercise|mindfulness>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String topic = String.valueOf(args.get("topic")).trim().toLowerCase();
                String res = switch (topic) {
                    case "sleep" -> "Resource: Campus Sleep Hygiene Workshop (Every Tuesday at 4 PM).";
                    case "exercise" -> "Resource: Free student gym pass at Campus Center Gym.";
                    default -> "Resource: Guided Meditation Sessions in Room 302.";
                };
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return res; }
                };
            }
        });

        String systemPrompt = "You are MindBridge, a supportive and empathetic campus wellness advisor.\n" +
                "You have access to these tools:\n" +
                "- wellness.recommend_activity: mood=<stressed|tired|anxious|unmotivated>\n" +
                "- wellness.get_resource: topic=<sleep|exercise|mindfulness>\n\n" +
                "If the user asks for resource locations, gym access, sleep help, or activity recommendations, respond ONLY with the tool call (e.g. wellness.get_resource|topic=mindfulness).\n" +
                "Otherwise, if the user is saying hello, asking follow-up questions, or chatting, respond with a warm, supportive message. Do NOT call any tools or output tool format.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            System.out.println("\nChat with MindBridge (e.g., 'I feel stressed', 'where can I meditate?', 'hello', type 'exit' to quit):");

            while (true) {
                System.out.print("\nUser > ");
                String input = reader.readLine();
                if (input == null || input.trim().equalsIgnoreCase("exit")) {
                    break;
                }
                if (input.trim().isEmpty()) continue;

                CHAT_LOG.append("User: ").append(input).append("\n");

                // Run agent turn
                agent.run(input);

                // If a tool was executed, run a second turn to generate the conversational response
                String lastMsg = bot.getHistory().messages().getLast().text();
                if (lastMsg.startsWith("Tool Execution Result")) {
                    agent.run("Based on the tool execution result, write a warm, supportive response to the user.");
                    lastMsg = bot.getHistory().messages().getLast().text();
                }

                // Print the final conversational answer
                System.out.println("\n🤖 MindBridge: " + lastMsg);
                CHAT_LOG.append("MindBridge: ").append(lastMsg).append("\n\n");
                
                // Save logs
                Files.createDirectories(LOG_PATH.getParent());
                Files.writeString(LOG_PATH, CHAT_LOG.toString());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
