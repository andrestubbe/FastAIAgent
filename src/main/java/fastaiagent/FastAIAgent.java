package fastaiagent;

import fastai.AI;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;
import fastansi.FastANSI;
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
        System.out.println(FastANSI.FG_YELLOW + "Agent Goal: " + goal + FastANSI.RESET);
        
        // 1. Plan
        StringBuilder toolsDef = new StringBuilder();
        for (fastairuntime.FastTool tool : runtime.getRegisteredTools()) {
            toolsDef.append("- ").append(tool.name()).append("\n");
        }

        String planPrompt = "You are a planner. Convert this goal: '" + goal + "' into a structured tool call command.\n" +
                            "Available tools:\n" +
                            toolsDef.toString() +
                            "Provide your step-by-step thinking inside <thoughts></thoughts> blocks first, then output the final tool call.\n" +
                            "Do not wrap the final tool call in any code blocks or custom tags like <tool_call>.\n" +
                            "Final Tool Call Output format: tool_name|arg_key=arg_value. Example: windows.open_app|path=notepad.exe";
        
        String planRaw = brain.ask(planPrompt).trim();
        String checkRaw = planRaw.toLowerCase()
                                 .replace("\\u003cthoughts\\u003e", "<thoughts>")
                                 .replace("\\u003c/thoughts\\u003e", "</thoughts>")
                                 .replace("\u003cthoughts\u003e", "<thoughts>")
                                 .replace("\u003c/thoughts\u003e", "</thoughts>");

        String normalizedRaw = planRaw.replace("\\u003cthoughts\\u003e", "<thoughts>")
                                       .replace("\\u003c/thoughts\\u003e", "</thoughts>")
                                       .replace("\u003cthoughts\u003e", "<thoughts>")
                                       .replace("\u003c/thoughts\u003e", "</thoughts>")
                                       .replace("\\u003cThoughts\\u003e", "<thoughts>")
                                       .replace("\\u003c/Thoughts\\u003e", "</thoughts>");

        System.out.println(FastANSI.FG_CYAN + "--- Planner Thought Trace ---" + FastANSI.RESET);
        if (normalizedRaw.contains("<thoughts>") && normalizedRaw.contains("</thoughts>")) {
            int start = normalizedRaw.indexOf("<thoughts>") + 10;
            int end = normalizedRaw.indexOf("</thoughts>");
            System.out.println(FastANSI.FG_BRIGHT_BLACK + normalizedRaw.substring(start, end).trim() + FastANSI.RESET);
        } else if (normalizedRaw.toLowerCase().contains("<thoughts>") && normalizedRaw.toLowerCase().contains("</thoughts>")) {
            int start = normalizedRaw.toLowerCase().indexOf("<thoughts>") + 10;
            int end = normalizedRaw.toLowerCase().indexOf("</thoughts>");
            System.out.println(FastANSI.FG_BRIGHT_BLACK + normalizedRaw.substring(start, end).trim() + FastANSI.RESET);
        } else {
            System.out.println(FastANSI.FG_BRIGHT_BLACK + normalizedRaw + FastANSI.RESET);
        }
        System.out.println(FastANSI.FG_CYAN + "-----------------------------" + FastANSI.RESET);

        // Parse generated command (extract last line or clean tool call format)
        String planLine = normalizedRaw;
        if (normalizedRaw.contains("</thoughts>")) {
            planLine = normalizedRaw.substring(normalizedRaw.indexOf("</thoughts>") + 11).trim();
        } else if (normalizedRaw.toLowerCase().contains("</thoughts>")) {
            planLine = normalizedRaw.substring(normalizedRaw.toLowerCase().indexOf("</thoughts>") + 11).trim();
        }
        
        // Strip custom XML tags if model generated them
        planLine = planLine.replace("<tool_call>", "")
                           .replace("</tool_call>", "")
                           .replace("\\u003ctool_call\\u003e", "")
                           .replace("\\u003c/tool_call\\u003e", "")
                           .replace("`", "")
                           .trim();
                           
        System.out.println(FastANSI.FG_GREEN + "Extracted Command: " + planLine + FastANSI.RESET);

        // Parse generated command(s) line-by-line for multi-step execution
        String[] lines = planLine.split("\n");
        for (String line : lines) {
            String cleanLine = line.trim();
            if (cleanLine.isEmpty()) continue;
            
            String[] parts = cleanLine.split("\\|");
            if (parts.length == 2) {
                String toolName = parts[0].trim();
                
                // Whitelist verification: Only run if it matches a registered tool
                boolean isValidTool = false;
                for (fastairuntime.FastTool registeredTool : runtime.getRegisteredTools()) {
                    if (registeredTool.name().equals(toolName)) {
                        isValidTool = true;
                        break;
                    }
                }
                
                if (!isValidTool) {
                    continue; // Skip thoughts or narrative generated by local models
                }
                
                Map<String, Object> args = new HashMap<>();
                
                // Handle multiple comma separated arguments: arg1=val1,arg2=val2
                String[] argPairs = parts[1].split(",");
                for (String pair : argPairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        args.put(kv[0].trim(), kv[1].trim());
                    }
                }

                if (!args.isEmpty()) {
                    System.out.println("Executing command step: " + toolName + " with " + args);
                    // 2. Act
                    FastCommand cmd = new FastCommand(toolName, args);
                    FastObservation obs = runtime.execute(cmd);

                    // 3. Observe
                    System.out.println("Step Observation: success=" + obs.success() + ", msg=" + obs.message());
                    memory.user(goal);
                    memory.assistant(obs.message());
                }
            }
        }
    }
}
