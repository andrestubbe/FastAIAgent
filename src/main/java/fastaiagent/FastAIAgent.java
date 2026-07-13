package fastaiagent;

import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FastAIAgent {

    private final FastAIBot bot;
    private final FastAIRuntime runtime;
    private final AgentLogger logger;

    private static final AgentLogger NO_OP_LOGGER = new AgentLogger() {
        @Override public void onGoal(String goal) {}
        @Override public void onThoughts(String thoughts) {}
        @Override public void onCommand(String command) {}
        @Override public void onActiveStep(String tool, Map<String, Object> args) {}
        @Override public void onObservation(boolean success, String message) {}
    };

    public FastAIAgent(FastAIBot bot, FastAIRuntime runtime) {
        this(bot, runtime, NO_OP_LOGGER);
    }

    public FastAIAgent(FastAIBot bot, FastAIRuntime runtime, AgentLogger logger) {
        this.bot = bot;
        this.runtime = runtime;
        this.logger = logger != null ? logger : NO_OP_LOGGER;
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
        Matcher m = Pattern.compile("([a-zA-Z0-9_]+)=").matcher(argsRaw);
        String currentKey = null;
        int lastValStart = -1;
        
        while (m.find()) {
            if (currentKey != null) {
                String val = argsRaw.substring(lastValStart, m.start()).trim();
                if (val.endsWith(",")) val = val.substring(0, val.length() - 1).trim();
                if (val.endsWith("|")) val = val.substring(0, val.length() - 1).trim();
                val = val.replaceAll("^['\"]|['\"]$", "");
                args.put(currentKey, decodeArg(val));
            }
            currentKey = m.group(1);
            lastValStart = m.end();
        }
        if (currentKey != null) {
            String val = argsRaw.substring(lastValStart).trim();
            if (val.endsWith("|raw")) val = val.substring(0, val.length() - 4).trim();
            if (val.endsWith("|")) val = val.substring(0, val.length() - 1).trim();
            val = val.replaceAll("^['\"]|['\"]$", "");
            args.put(currentKey, decodeArg(val));
        }

        return new ParsedCommand(tool, args);
    }

    private static String decodeArg(String val) {
        try {
            return URLDecoder.decode(val, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return val;
        }
    }

    private record ParsedCommand(String tool, Map<String,Object> args) {}

    public void run(String goal) {
        logger.onGoal(goal);

        bot.streamChat(goal);

        String raw = bot.getHistory().messages().getLast().text().trim();
        String normalized = normalize(raw);

        String thoughts = extractThoughts(normalized);
        if (thoughts != null) {
            logger.onThoughts(thoughts);
        }

        String cmdBlock = extractCommandBlock(normalized);
        logger.onCommand(cmdBlock);

        ParsedCommand pc = parseCommand(cmdBlock);
        if (pc != null) {
            boolean valid = runtime.getRegisteredTools().stream().anyMatch(t -> t.name().equals(pc.tool()));
            if (valid) {
                logger.onActiveStep(pc.tool(), pc.args());

                FastCommand cmd = new FastCommand(pc.tool(), pc.args());
                FastObservation obs = runtime.execute(cmd);

                logger.onObservation(obs.success(), obs.message());
                
                bot.getHistory().user("Tool Execution Result (" + pc.tool() + "): " + obs.message());
            }
        }
    }
}
