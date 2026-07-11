package fastaiagent;

import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;
import fastansi.FastANSI;
import fastemojis.FastEmojis;

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

    private final FastAIBot bot;
    private final FastAIRuntime runtime;

    public FastAIAgent(FastAIBot bot, FastAIRuntime runtime) {
        this.bot = bot;
        this.runtime = runtime;
    }

    public void run(String goal) {
        System.out.println(TN_GOAL + FastEmojis.ROBOT + "  Goal: " + goal + FastANSI.RESET);
        
        // 1. Plan
        StringBuilder toolsDef = new StringBuilder();
        for (fastairuntime.FastTool tool : runtime.getRegisteredTools()) {
            toolsDef.append("- ").append(tool.name()).append("\n");
        }

        String planPrompt = "You are a fast AI Agent. Convert this goal: '" + goal + "' into a single structured tool call.\n" +
                            "Available tools:\n" +
                            toolsDef.toString() +
                            "For file saving, use: file.save|path=<file_path>,content=<text_to_save>\n" +
                            "For typing, use: keyboard.type|text=<text_to_type>\n" +
                            "For opening apps, use: windows.open_app|path=<executable_path>\n" +
                            "Always answer with the precise tool call in plain text (do NOT wrap it in markdown code blocks). Give no explanation.\n" +
                            "Output format: tool_name|arg_key=arg_value. Example:\n" +
                            "file.save|path=target/reasoning_output.txt,content=Executed successfully.";
        
        // Clear previous response string builder conceptually (FastAIBot adds to its history)
        bot.streamChat(planPrompt);

        // Get the latest response from FastAIBot's internal history
        String planRaw = bot.getHistory().messages().get(bot.getHistory().messages().size() - 1).text().trim();
        String normalizedRaw = planRaw.replace("\\u003cthoughts\\u003e", "<thoughts>")
                                       .replace("\\u003c/thoughts\\u003e", "</thoughts>")
                                       .replace("\u003cthoughts\u003e", "<thoughts>")
                                       .replace("\u003c/thoughts\u003e", "</thoughts>")
                                       .replace("\\u003cThoughts\\u003e", "<thoughts>")
                                       .replace("\\u003c/Thoughts\\u003e", "</thoughts>");

        if (normalizedRaw.contains("<thoughts>") && normalizedRaw.contains("</thoughts>")) {
            System.out.println(TN_HEADER + FastEmojis.THINKING + "  " + FastEmojis.BOX_ROUND_TOP_LEFT + FastEmojis.BOX_HORIZONTAL + " Thought Trace " + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_ROUND_TOP_RIGHT + FastANSI.RESET);
            int start = normalizedRaw.indexOf("<thoughts>") + 10;
            int end = normalizedRaw.indexOf("</thoughts>");
            System.out.println(TN_THOUGHT + normalizedRaw.substring(start, end).trim() + FastANSI.RESET);
            System.out.println(TN_HEADER + "   " + FastEmojis.BOX_ROUND_BOTTOM_LEFT + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_ROUND_BOTTOM_RIGHT + FastANSI.RESET);
        } else if (normalizedRaw.toLowerCase().contains("<thoughts>") && normalizedRaw.toLowerCase().contains("</thoughts>")) {
            System.out.println(TN_HEADER + FastEmojis.THINKING + "  " + FastEmojis.BOX_ROUND_TOP_LEFT + FastEmojis.BOX_HORIZONTAL + " Thought Trace " + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_ROUND_TOP_RIGHT + FastANSI.RESET);
            int start = normalizedRaw.toLowerCase().indexOf("<thoughts>") + 10;
            int end = normalizedRaw.toLowerCase().indexOf("</thoughts>");
            System.out.println(TN_THOUGHT + normalizedRaw.substring(start, end).trim() + FastANSI.RESET);
            System.out.println(TN_HEADER + "   " + FastEmojis.BOX_ROUND_BOTTOM_LEFT + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_HORIZONTAL + FastEmojis.BOX_ROUND_BOTTOM_RIGHT + FastANSI.RESET);
        }
        // If there's no thought trace, we just silently skip rendering the box.

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
                           
        System.out.println(TN_CMD + FastEmojis.LIGHTNING + "  " + planLine + FastANSI.RESET);

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
                    System.out.println(TN_STEP + FastEmojis.GEAR + "  " + toolName + " " + args + FastANSI.RESET);
                    // 2. Act
                    FastCommand cmd = new FastCommand(toolName, args);
                    FastObservation obs = runtime.execute(cmd);

                    // 3. Observe
                    String icon = obs.success() ? FastEmojis.CHECK : FastEmojis.ERROR_RED;
                    System.out.println(TN_OBS + "   " + icon + "  " + obs.message() + FastANSI.RESET);
                    // Remove manual tracking, FastAIBot already holds context!
                    // Let's just output the observation.
                    bot.getHistory().user("Tool Execution Result (" + toolName + "): " + obs.message());
                }
            }
        }
    }
}
