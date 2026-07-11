package fastaiagent;

import fastai.AI;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;
import java.util.HashMap;
import java.util.Map;

public final class FastAIAgent {

    private final AI brain;
    private final FastAIRuntime runtime;
    private final ConversationHistory memory;

    public FastAIAgent(AI brain, FastAIRuntime runtime, ConversationHistory memory) {
        this.brain = brain;
        this.runtime = runtime;
        this.memory = memory;
    }

    public void run(String goal) {
        System.out.println("Agent Goal: " + goal);
        
        // 1. Plan (Simplification for demonstration using LLM mapping to runtime tools)
        String planPrompt = "You are a planner. Convert this goal: '" + goal + "' into a structured tool call command.\n" +
                            "Available tools:\n" +
                            "- windows.open_app (args: path)\n" +
                            "- keyboard.type (args: text)\n" +
                            "Output format: tool_name|arg_key=arg_value. Example: windows.open_app|path=notepad.exe\n" +
                            "Only output the format string, no explanations.";
        
        String planRaw = brain.ask(planPrompt).trim();
        System.out.println("Planner generated command: " + planRaw);

        // Parse generated command
        String[] parts = planRaw.split("\\|");
        if (parts.length == 2) {
            String toolName = parts[0];
            String[] argParts = parts[1].split("=");
            if (argParts.length == 2) {
                Map<String, Object> args = new HashMap<>();
                args.put(argParts[0], argParts[1]);

                // 2. Act
                FastCommand cmd = new FastCommand(toolName, args);
                FastObservation obs = runtime.execute(cmd);

                // 3. Observe
                System.out.println("Execution Observation: success=" + obs.success() + ", msg=" + obs.message());
                memory.user(goal);
                memory.assistant(obs.message());
            }
        }
    }
}
