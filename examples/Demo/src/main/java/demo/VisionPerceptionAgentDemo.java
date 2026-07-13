package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastairuntime.tools.FileSaveTool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

/**
 * Demo referencing Chapter 11: Multi-Modal Perception Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Vision-Language Perception Agent using Ollama Moondream.
 */
public final class VisionPerceptionAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Vision-Language Perception Agent Demo ===");

        // Generate a sample image with growth chart representation for testing
        String imagePath = "target/sample_chart.png";
        try {
            Files.createDirectories(Path.of("target"));
            BufferedImage img = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 200, 100);
            g.setColor(Color.GREEN);
            g.drawLine(10, 80, 70, 60);
            g.drawLine(70, 60, 130, 40);
            g.drawLine(130, 40, 190, 10);
            g.drawString("UPWARD TREND", 20, 30);
            g.dispose();
            ImageIO.write(img, "png", new File(imagePath));
            System.out.println("Generated sample chart image at: " + imagePath);
        } catch (Exception e) {
            System.out.println("Failed to generate sample image: " + e.getMessage());
        }

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Vision perception only)
        FastAIRuntime visionRuntime = new FastAIRuntime();
        visionRuntime.register(new FastTool() {
            @Override public String name() { return "vision.analyze_image"; }
            @Override public String description() { return "path=<image_path>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String pathStr = String.valueOf(args.get("path")).trim();
                System.out.println("Reading image and querying local moondream model...");
                try {
                    byte[] fileContent = Files.readAllBytes(Path.of(pathStr));
                    String base64Image = Base64.getEncoder().encodeToString(fileContent);

                    // HTTP query to local Ollama instance for Moondream
                    URL url = new URL("http://localhost:11434/api/generate");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    String payload = "{\n" +
                            "  \"model\": \"moondream\",\n" +
                            "  \"prompt\": \"Describe the trend shown in this line chart.\",\n" +
                            "  \"stream\": false,\n" +
                            "  \"images\": [\"" + base64Image + "\"]\n" +
                            "}";

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    if (conn.getResponseCode() == 200) {
                        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        // Extract "response" value from JSON
                        String desc = response;
                        if (response.contains("\"response\":\"")) {
                            int start = response.indexOf("\"response\":\"") + 12;
                            int end = response.indexOf("\"", start);
                            desc = response.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
                        }
                        String finalDesc = desc;
                        return new FastObservation() {
                            @Override public boolean success() { return true; }
                            @Override public String message() { return finalDesc; }
                        };
                    }
                } catch (Exception e) {
                    System.out.println("Moondream query failed (falling back to mock perception): " + e.getMessage());
                }

                // Fallback mock description if moondream is not available
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "A line chart showing a clear upward trend from bottom-left to top-right with label 'UPWARD TREND'."; }
                };
            }
        });

        String visionSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(visionRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot visionBot = new FastAIBot(brain, visionSystemPrompt, out, out);
        FastAIAgent visionAgent = new FastAIAgent(visionBot, visionRuntime, new TokyoNightLogger());

        // Run Step 1: Perception
        System.out.println("\n--- STEP 1: PERCEIVING THE CHART IMAGE ---");
        visionAgent.run("Analyze the image target/sample_chart.png");

        String perceptionOutput = visionBot.getHistory().messages().getLast().text();
        if (perceptionOutput.contains("Tool Execution Result (vision.analyze_image): ")) {
            perceptionOutput = perceptionOutput.replace("Tool Execution Result (vision.analyze_image): ", "").trim();
        }

        // 2. Step 2 Runtime (Report saving only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        String saveSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, saveSystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Run Step 2: Saving Report
        System.out.println("\n--- STEP 2: SAVING PERCEPTION REPORT ---");
        saveAgent.run(String.format(
            "file.save|path=target/perception_report.txt,content=Vision Perception Report: The chart shows:\n%s",
            perceptionOutput
        ));
    }
}
