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

    // Color palette — blues & yellows
    private static final String TN_GOAL    = FastANSI.fg(255, 215,   0); // vivid gold
    private static final String TN_HEADER  = FastANSI.fg(122, 162, 247); // bright blue  #7aa2f7
    private static final String TN_THOUGHT = FastANSI.fg(125, 207, 255); // sky blue     #7dcfff
    private static final String TN_CMD     = FastANSI.fg(255, 158, 100); // amber orange #ff9e64
    private static final String TN_STEP    = FastANSI.fg(224, 175, 104); // warm gold    #e0af68
    private static final String TN_OBS     = FastANSI.fg(137, 221, 255); // ice blue     #89ddff

    private final AI brain;
    private final FastAIRuntime runtime;
    private final ConversationHistory memory;

    public FastAIAgent(AI brain, FastAIRuntime runtime, ConversationHistory memory) {
        this.brain = brain;
        this.runtime = runtime;
        this.memory = memory;
    }

    public void run(String goal) {
        System.out.println(TN_GOAL + "Agent Goal: " + goal + FastANSI.RESET);
        
        // 1. Plan
        StringBuilder toolsDef = new StringBuilder();
        for (fastairuntime.FastTool tool : runtime.getRegisteredTools()) {
            toolsDef.append("- ").append(tool.name()).append("\n");
        }

        String planPrompt = "You are a planner. Convert this goal: '" + goal + "' into structured tool call commands.\n" +
                            "Available tools:\n" +
                            toolsDef.toString() +
                            "For file saving, use: file.save|path=<file_path>,content=<text_to_save>\n" +
                            "For typing, use: keyboard.type|text=<text_to_type>\n" +
                            "For opening apps, use: windows.open_app|path=<executable_path>\n" +
                            "Provide your step-by-step thinking inside <thoughts></thoughts> blocks first, then output the final tool calls.\n" +
                            "Final Tool Call Output format: tool_name|arg_key=arg_value. Example:\n" +
                            "file.save|path=target/reasoning_output.txt,content=Executed multi-step logic successfully.";
        
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

        System.out.println(TN_HEADER + "--- Planner Thought Trace ---" + FastANSI.RESET);
        if (normalizedRaw.contains("<thoughts>") && normalizedRaw.contains("</thoughts>")) {
            int start = normalizedRaw.indexOf("<thoughts>") + 10;
            int end = normalizedRaw.indexOf("</thoughts>");
            System.out.println(TN_THOUGHT + normalizedRaw.substring(start, end).trim() + FastANSI.RESET);
        } else if (normalizedRaw.toLowerCase().contains("<thoughts>") && normalizedRaw.toLowerCase().contains("</thoughts>")) {
            int start = normalizedRaw.toLowerCase().indexOf("<thoughts>") + 10;
            int end = normalizedRaw.toLowerCase().indexOf("</thoughts>");
            System.out.println(TN_THOUGHT + normalizedRaw.substring(start, end).trim() + FastANSI.RESET);
        } else {
            System.out.println(TN_THOUGHT + normalizedRaw + FastANSI.RESET);
        }
        System.out.println(TN_HEADER + "-----------------------------" + FastANSI.RESET);

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
                           .replace("<tool_name>", "")
                           .replace("</tool_name>", "")
                           .replace("\\u003ctool_call\\u003e", "")
                           .replace("\\u003c/tool_call\\u003e", "")
                           .replace("\\u003ctool_name\\u003e", "")
                           .replace("\\u003c/tool_name\\u003e", "")
                           .replace("`", "")
                           .trim();
                           
        System.out.println(TN_CMD + "Extracted Command: " + planLine + FastANSI.RESET);

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
                // Split on | with limit 2 to preserve | in values
                String argsRaw = cleanLine.split("\\|", 2)[1];
                String[] argPairs = argsRaw.split(",");
                for (String pair : argPairs) {
                    // Split on first = only (values may contain = signs)
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String val = kv[1].trim()
                                          .replaceAll("^\"|\"$", "")  // strip surrounding "
                                          .replaceAll("^'|'$", "");   // strip surrounding '
                        args.put(key, val);
                    }
                }

                if (!args.isEmpty()) {
                    System.out.println(TN_STEP + "Executing: " + toolName + " " + args + FastANSI.RESET);
                    // 2. Act
                    FastCommand cmd = new FastCommand(toolName, args);
                    FastObservation obs = runtime.execute(cmd);

                    // 3. Observe
                    System.out.println(TN_OBS + "  " + (obs.success() ? "[OK]  " : "[FAIL]") + " " + obs.message() + FastANSI.RESET);
                    memory.user(goal);
                    memory.assistant(obs.message());
                }
            }
        }
    }
}
