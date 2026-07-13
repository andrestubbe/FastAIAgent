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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 9: Software Development Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Smart Home Controller Agent (State-Management Workflow).
 */
public final class SmartHomeControllerDemo {

    private static final Map<String, String> DEVICES = new LinkedHashMap<>();
    private static final Path JSON_PATH = Path.of("target/smart_home_state.json");

    public static void main(String[] args) {
        System.out.println("=== Running Smart Home Controller Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // Setup Runtime
        FastAIRuntime runtime = new FastAIRuntime();

        // 1. Set Device State Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "home.set_device_state"; }
            @Override public String description() { return "device=<light|tv|ac>,state=<ON|OFF|percentage|temperature>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String device = String.valueOf(args.get("device")).trim().toLowerCase();
                String state = String.valueOf(args.get("state")).trim().toUpperCase();
                
                if (!DEVICES.containsKey(device)) {
                    return new FastObservation() {
                        @Override public boolean success() { return false; }
                        @Override public String message() { return "Device '" + device + "' is not registered."; }
                    };
                }
                
                DEVICES.put(device, state);
                saveState();
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Set device " + device + " to state: " + state; }
                };
            }
        });

        // 2. List Devices Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "home.list_devices"; }
            @Override public String description() { return "no arguments"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                StringBuilder sb = new StringBuilder("Smart Home Device Board:\n");
                for (Map.Entry<String, String> entry : DEVICES.entrySet()) {
                    sb.append(String.format("- %s: %s\n", entry.getKey().toUpperCase(), entry.getValue()));
                }
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return sb.toString(); }
                };
            }
        });

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        // Default seeds
        DEVICES.put("light", "OFF");
        DEVICES.put("tv", "OFF");
        DEVICES.put("ac", "OFF");
        saveState();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            System.out.println("\nCommands (e.g., 'Turn on the TV', 'Dim light to 50%', 'Set AC to 22C', 'list devices', type 'exit' to quit):");

            while (true) {
                System.out.print("\nCommand Input > ");
                String cmd = reader.readLine();
                if (cmd == null || cmd.trim().equalsIgnoreCase("exit")) {
                    break;
                }
                if (cmd.trim().isEmpty()) continue;

                agent.run(cmd);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void saveState() {
        try {
            Files.createDirectories(JSON_PATH.getParent());
            StringBuilder json = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<String, String> entry : DEVICES.entrySet()) {
                json.append(String.format("  \"%s\": \"%s\"", entry.getKey(), entry.getValue()));
                if (++i < DEVICES.size()) json.append(",");
                json.append("\n");
            }
            json.append("}");
            Files.writeString(JSON_PATH, json.toString());
        } catch (Exception ignored) {}
    }
}
