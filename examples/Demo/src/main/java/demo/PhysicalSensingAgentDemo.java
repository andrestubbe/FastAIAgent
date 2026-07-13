package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 11: Multi-Modal Perception Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Physical World Sensing Agent (reads thermometer, triggers AC).
 */
public final class PhysicalSensingAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Physical World Sensing Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Sensor Reading only)
        FastAIRuntime sensorRuntime = new FastAIRuntime();
        sensorRuntime.register(new FastTool() {
            @Override public String name() { return "sensor.read_temperature"; }
            @Override public String description() { return "no arguments"; }
            @Override public fastairuntime.FastObservation execute(Map<String, Object> args) {
                System.out.println("Reading temperature sensor...");
                String tempReading = "28C";
                return new fastairuntime.FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return tempReading; }
                };
            }
        });

        String sensorSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(sensorRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot sensorBot = new FastAIBot(brain, sensorSystemPrompt, out, out);
        FastAIAgent sensorAgent = new FastAIAgent(sensorBot, sensorRuntime, new TokyoNightLogger());

        // Run Step 1: Read Sensor
        System.out.println("\n--- STEP 1: READING SENSOR VALUE ---");
        sensorAgent.run("Read the temperature sensor.");

        String tempVal = sensorBot.getHistory().messages().getLast().text();
        if (tempVal.contains("Tool Execution Result (sensor.read_temperature): ")) {
            tempVal = tempVal.replace("Tool Execution Result (sensor.read_temperature): ", "").trim();
        }

        // 2. Step 2 Runtime (Smart Home Control only)
        FastAIRuntime homeRuntime = new FastAIRuntime();
        homeRuntime.register(new FastTool() {
            @Override public String name() { return "home.set_device_state"; }
            @Override public String description() { return "device=<ac|fan>,state=<ON|OFF|temperature>"; }
            @Override public fastairuntime.FastObservation execute(Map<String, Object> args) {
                String device = String.valueOf(args.get("device")).trim().toLowerCase();
                String state = String.valueOf(args.get("state")).trim().toUpperCase();
                System.out.println(String.format("Climate Control Activated -> Device: %s, State: %s", device, state));
                return new fastairuntime.FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Set device " + device + " to state " + state; }
                };
            }
        });

        String homeSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(homeRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot homeBot = new FastAIBot(brain, homeSystemPrompt, out, out);
        FastAIAgent homeAgent = new FastAIAgent(homeBot, homeRuntime, new TokyoNightLogger());

        // Run Step 2: Decide Control Action
        System.out.println("\n--- STEP 2: EVALUATING THRESHOLD & CONTROLLING CLIMATE ---");
        homeAgent.run(String.format("The current temperature is %s. If it is above 25C, turn on the ac.", tempVal));
    }
}
