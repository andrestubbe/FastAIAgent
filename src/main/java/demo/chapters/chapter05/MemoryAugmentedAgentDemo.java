package demo.chapters.chapter05;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;

/**
 * Demo referencing Chapter 5: Foundational Cognitive Architectures
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: The Memory-Augmented Agent maintaining context across multiple steps.
 */
public final class MemoryAugmentedAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Memory-Augmented Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: GEMINI_API_KEY environment variable is not set.");
            return;
        }

        AI brain = FastAI.connect("gemini:gemini-2.5-flash", apiKey);
        
        // Dynamic memory state tracker
        ConversationHistory memory = new ConversationHistory();
        FastAIAgent agent = new FastAIAgent(brain, runtime, memory);

        // Sequence Step 1
        System.out.println("\n--- STEP 1 ---");
        agent.run("Start application notepad.exe");

        // Sequence Step 2 (requires memory of the open app context)
        System.out.println("\n--- STEP 2 ---");
        agent.run("Type 'Hello from Memory-Augmented Agent!' inside the open window.");
    }
}
