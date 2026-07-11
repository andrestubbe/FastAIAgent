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
        
        // 1. Plan
        StringBuilder toolsDef = new StringBuilder();
        for (fastairuntime.FastTool tool : runtime.getRegisteredTools()) {
            toolsDef.append("- ").append(tool.name()).append("\n");
        }

        String planPrompt = "You are a planner. Convert this goal: '" + goal + "' into a structured tool call command.\n" +
                            "Available tools:\n" +
                            toolsDef.toString() +
                            "Provide your step-by-step thinking inside <thoughts></thoughts> blocks first, then output the final tool call.\n" +
                            "Final Tool Call Output format: tool_name|arg_key=arg_value. Example: windows.open_app|path=notepad.exe";
        
        String planRaw = brain.ask(planPrompt).trim();
        // Normalise unicode escaped tags if returned in raw streams
        String cleanRaw = planRaw.replace("\\u003cthoughts\\u003e", "<thoughts>")
                                 .replace("\\u003c/thoughts\\u003e", "</thoughts>")
                                 .replace("\u003cthoughts\u003e", "<thoughts>")
                                 .replace("\u003c/thoughts\u003e", "</thoughts>");

        System.out.println("--- Planner Thought Trace ---");
        if (cleanRaw.contains("<thoughts>") && cleanRaw.contains("</thoughts>")) {
            int start = cleanRaw.indexOf("<thoughts>") + 10;
            int end = cleanRaw.indexOf("</thoughts>");
            System.out.println(cleanRaw.substring(start, end).trim());
        } else {
            System.out.println(cleanRaw);
        }
        System.out.println("-----------------------------");

        // Parse generated command (extract last line or clean tool call format)
        String planLine = cleanRaw;
        if (cleanRaw.contains("</thoughts>")) {
            planLine = cleanRaw.substring(cleanRaw.indexOf("</thoughts>") + 11).trim();
        }
        System.out.println("Extracted Command: " + planLine);

        // Parse generated command
        String[] parts = planLine.split("\\|");
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
