package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.KeyboardTypeTool;
import fastairuntime.tools.WindowsAppTool;

public final class AgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Starting FastAIAgent Cognitive Loop Demo ===");

        // Setup Runtime
        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new WindowsAppTool());
        runtime.register(new KeyboardTypeTool());

        // Connect Brain via Ollama (Llama 3.2 3B)
        AI brain = FastAI.connect("ollama:llama3.2:3b");
        ConversationHistory memory = new ConversationHistory();

        FastAIAgent agent = new FastAIAgent(brain, runtime, memory);

        // Execute task: Plan -> Act -> Observe
        agent.run("Start application notepad.exe");
    }
}
