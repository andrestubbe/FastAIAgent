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
 */
public final class PlanningAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Chapter 05: Planning Agent Demo ===");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());

        AI brain = FastAI.connect("ollama:llama3.2:3b");
        ConversationHistory memory = new ConversationHistory();

        FastAIAgent agent = new FastAIAgent(brain, runtime, memory);
        agent.run("Start application notepad.exe");
    }
}
