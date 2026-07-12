package fastaiagent;

import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastansi.FastANSI;
import fastemojis.FastEmojis;

import java.util.HashMap;
import java.util.Map;

public final class FastAIAgent {

    // 🎨 Tokyo Night Full Palette (v2)
    private static final String TN_BG_DARK    = FastANSI.bg(26, 27, 38);   // #1a1b26
    private static final String TN_BG_DARKER  = FastANSI.bg(22, 22, 30);   // #16161e
    private static final String TN_BG_SIDE    = FastANSI.bg(31, 35, 53);   // #1f2335
    private static final String TN_BG_HL      = FastANSI.bg(47, 51, 77);   // #2f334d
    private static final String TN_BG_ERROR   = FastANSI.bg(247, 118, 142); // #f7768e (Error Invert)

    private static final String TN_FG         = FastANSI.fg(192, 202, 245); // #c0caf5
    private static final String TN_FG_DIM     = FastANSI.fg(169, 177, 214); // #a9b1d6
    private static final String TN_FG_DARK    = FastANSI.fg(86, 95, 137);   // #565f89
    private static final String TN_FG_BLACK   = FastANSI.fg(0, 0, 0);       // For inverted error

    private static final String TN_GOAL   = FastANSI.fg(255, 215, 0);   // Gold
    private static final String TN_HEADER = FastANSI.fg(122, 162, 247); // Blue
    private static final String TN_THOUGHT= FastANSI.fg(125, 207, 255); // Sky
    private static final String TN_CMD    = FastANSI.fg(255, 158, 100); // Orange
    private static final String TN_STEP   = FastANSI.fg(158, 206, 106); // Green (Active step)
    private static final String TN_OBS    = FastANSI.fg(180, 252, 255); // Ice

    private final FastAIBot bot;
    private final FastAIRuntime runtime;
    private long last = System.currentTimeMillis();

    public FastAIAgent(FastAIBot bot, FastAIRuntime runtime) {
        this.bot = bot;
        this.runtime = runtime;
    }

    private String tick() {
        long now = System.currentTimeMillis();
        long delta = now - last;
        last = now;
        return String.format("[%5d ms] ", delta);
    }

    private static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\\u003c", "<").replace("\\u003e", ">");
        s = s.replaceAll("(?i)<thoughts>", "<thoughts>").replaceAll("(?i)</thoughts>", "</thoughts>");
        s = s.replaceAll("(?i)</?tool_call>", "").replaceAll("(?i)</?tool_name>", "");
        return s.trim();
    }

    private static String extractThoughts(String normalized) {
        int start = normalized.indexOf("<thoughts>");
        int end   = normalized.indexOf("</thoughts>");
        if (start < 0 || end < 0) return null;
        return normalized.substring(start + 10, end).trim();
    }

    private static String extractCommandBlock(String normalized) {
        int end = normalized.indexOf("</thoughts>");
        if (end < 0) return normalized.trim();
        return normalized.substring(end + 11).trim();
    }

    private ParsedCommand parseCommand(String block) {
        String[] parts = block.split("\\|", 2);
        if (parts.length != 2) return null;

        String tool = parts[0].trim();
        if (tool.startsWith("tool_call=")) {
            tool = tool.substring(10).trim();
        }
        
        String argsRaw = parts[1].trim();

        Map<String, Object> args = new HashMap<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("([a-zA-Z0-9_]+)=").matcher(argsRaw);
        String currentKey = null;
        int lastValStart = -1;
        
        while (m.find()) {
            if (currentKey != null) {
                String val = argsRaw.substring(lastValStart, m.start()).trim();
                if (val.endsWith(",")) val = val.substring(0, val.length() - 1).trim();
                if (val.endsWith("|")) val = val.substring(0, val.length() - 1).trim();
                val = val.replaceAll("^['\"]|['\"]$", "");
                args.put(currentKey, val);
            }
            currentKey = m.group(1);
            lastValStart = m.end();
        }
        if (currentKey != null) {
            String val = argsRaw.substring(lastValStart).trim();
            if (val.endsWith("|raw")) val = val.substring(0, val.length() - 4).trim();
            if (val.endsWith("|")) val = val.substring(0, val.length() - 1).trim();
            val = val.replaceAll("^['\"]|['\"]$", "");
            args.put(currentKey, val);
        }

        return new ParsedCommand(tool, args);
    }

    private record ParsedCommand(String tool, Map<String,Object> args) {}

    // --- V2 Rendering Helpers ---

    private void printNoiseLine() {
        System.out.println(TN_BG_DARKER + TN_FG_DARK + "  " + FastEmojis.BOX_HORIZONTAL.repeat(60) + FastANSI.RESET);
    }

    private void printPanelFrame(String title, String content, String colorBg, String colorFg) {
        System.out.println(colorBg + colorFg + "  " + FastEmojis.BOX_ROUND_TOP_LEFT + FastEmojis.BOX_HORIZONTAL.repeat(2) + " " + title + " " + FastEmojis.BOX_HORIZONTAL.repeat(40 - title.length()) + FastEmojis.BOX_ROUND_TOP_RIGHT + "  " + FastANSI.RESET);
        for (String line : content.split("\n")) {
            System.out.println(colorBg + colorFg + "  " + FastEmojis.BOX_VERTICAL + " " + String.format("%-44s", line) + FastEmojis.BOX_VERTICAL + "  " + FastANSI.RESET);
        }
        System.out.println(colorBg + colorFg + "  " + FastEmojis.BOX_ROUND_BOTTOM_LEFT + FastEmojis.BOX_HORIZONTAL.repeat(44) + FastEmojis.BOX_ROUND_BOTTOM_RIGHT + "  " + FastANSI.RESET);
    }

    public void run(String goal) {

        System.out.println("\n" + TN_BG_DARKER + TN_GOAL + FastEmojis.ROBOT + "  Goal: " + goal + " ".repeat(Math.max(0, 60 - goal.length())) + FastANSI.RESET);
        printNoiseLine();

        bot.streamChat(goal);

        String raw = bot.getHistory().messages().getLast().text().trim();
        String normalized = normalize(raw);

        // Thought trace Panel
        String thoughts = extractThoughts(normalized);
        if (thoughts != null) {
            printPanelFrame("Thought Trace", thoughts, TN_BG_SIDE, TN_THOUGHT);
        }

        // Command Highlight Step
        String cmdBlock = extractCommandBlock(normalized);
        System.out.println(tick() + TN_BG_HL + TN_CMD + FastEmojis.LIGHTNING + "  COMMAND: " + String.format("%-46s", cmdBlock) + FastANSI.RESET);

        ParsedCommand pc = parseCommand(cmdBlock);
        if (pc != null) {
            boolean valid = runtime.getRegisteredTools().stream().anyMatch(t -> t.name().equals(pc.tool()));
            if (valid) {
                // Active Step
                System.out.println(tick() + TN_BG_DARK + TN_STEP + FastEmojis.GEAR + "  " + String.format("%-55s", pc.tool() + " " + pc.args()) + FastANSI.RESET);

                FastCommand cmd = new FastCommand(pc.tool(), pc.args());
                FastObservation obs = runtime.execute(cmd);

                // Observation (Error-Invert or Dim Ice)
                if (obs.success()) {
                    System.out.println(tick() + TN_BG_DARKER + TN_OBS + FastEmojis.CHECK + "  " + String.format("%-55s", obs.message()) + FastANSI.RESET);
                } else {
                    // Error Invert Mode
                    System.out.println(tick() + TN_BG_ERROR + TN_FG_BLACK + FastEmojis.ERROR_RED + "  ERROR: " + String.format("%-49s", obs.message()) + FastANSI.RESET);
                }
                
                printNoiseLine();
                bot.getHistory().user("Tool Execution Result (" + pc.tool() + "): " + obs.message());
            }
        }
    }
}
