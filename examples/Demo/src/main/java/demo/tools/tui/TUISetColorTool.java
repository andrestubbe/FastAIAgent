package demo.tools.tui;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.util.Map;

public class TUISetColorTool implements FastTool {
    private final FastTUIContext context;

    public TUISetColorTool(FastTUIContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "tui.set_color";
    }

    public String description() {
        return "Sets the active foreground and background colors (hex format e.g. 0xFFFFFF). Args: fg, bg.";
    }

    private record Obs(boolean success, String message) implements FastObservation {}

    @Override
    public FastObservation execute(Map<String, Object> args) {
        try {
            String fgStr = args.containsKey("fg") ? String.valueOf(args.get("fg")) : (args.containsKey("foreground") ? String.valueOf(args.get("foreground")) : "0xc0caf5");
            String bgStr = args.containsKey("bg") ? String.valueOf(args.get("bg")) : (args.containsKey("background") ? String.valueOf(args.get("background")) : "0x1a1b26");
            int fg = Integer.decode(fgStr.replace(" ", ""));
            int bg = Integer.decode(bgStr.replace(" ", ""));
            context.setColor(fg, bg);
            return new Obs(true, "Colors set: fg=" + fg + ", bg=" + bg);
        } catch (Exception e) {
            return new Obs(false, "Failed to set colors: " + e.getMessage());
        }
    }
}
