package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 11: Multi-Modal Perception Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Audio Processing Agent (Voice-Activated Smart Home Assistant).
 */
public final class AudioProcessingAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Audio Processing Agent Demo ===");

        // Pre-create target and dummy wav file if not present
        String audioPath = "target/voice_cmd.wav";
        try {
            Files.createDirectories(Path.of("target"));
            Path p = Path.of(audioPath);
            if (!Files.exists(p)) {
                Files.write(p, new byte[100]); // tiny dummy wave placeholder
                System.out.println("Created dummy audio command file at: " + audioPath);
            }
        } catch (Exception ignored) {}

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Audio transcription only)
        FastAIRuntime audioRuntime = new FastAIRuntime();
        audioRuntime.register(new FastTool() {
            @Override public String name() { return "audio.transcribe"; }
            @Override public String description() { return "path=<audio_file>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String pathStr = String.valueOf(args.get("path")).trim();
                System.out.println("Transcribing audio file " + pathStr + "...");
                // Mock transcription output representing a spoken smart home command
                String transcript = "Turn on the light";
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return transcript; }
                };
            }
        });

        String audioSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(audioRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot audioBot = new FastAIBot(brain, audioSystemPrompt, out, out);
        FastAIAgent audioAgent = new FastAIAgent(audioBot, audioRuntime, new TokyoNightLogger());

        // Run Step 1: Transcription
        System.out.println("\n--- STEP 1: TRANSCRIBING AUDIO COMMAND ---");
        audioAgent.run("Transcribe the voice command audio file target/voice_cmd.wav");

        String transcript = audioBot.getHistory().messages().getLast().text();
        if (transcript.contains("Tool Execution Result (audio.transcribe): ")) {
            transcript = transcript.replace("Tool Execution Result (audio.transcribe): ", "").trim();
        }

        // 2. Step 2 Runtime (Smart Home Control only)
        FastAIRuntime homeRuntime = new FastAIRuntime();
        homeRuntime.register(new FastTool() {
            @Override public String name() { return "home.set_device_state"; }
            @Override public String description() { return "device=<light|tv|ac>,state=<ON|OFF|percentage|temperature>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String device = String.valueOf(args.get("device")).trim().toLowerCase();
                String state = String.valueOf(args.get("state")).trim().toUpperCase();
                System.out.println(String.format("Smart Home Triggered -> Device: %s, State: %s", device, state));
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Successfully turned " + state + " device " + device; }
                };
            }
        });

        String homeSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(homeRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot homeBot = new FastAIBot(brain, homeSystemPrompt, out, out);
        FastAIAgent homeAgent = new FastAIAgent(homeBot, homeRuntime, new TokyoNightLogger());

        // Run Step 2: Control Execution
        System.out.println("\n--- STEP 2: EXECUTING TRANSCRIBED COMMAND ---");
        homeAgent.run(String.format("Execute the transcribed voice command: '%s'", transcript));
    }
}
