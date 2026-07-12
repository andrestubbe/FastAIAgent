package demo.tools.tui;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.util.Map;

public class TUIDrawTextTool implements FastTool {
    private final FastTUIContext context;

    public TUIDrawTextTool(FastTUIContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "tui.draw_text";
    }

    public String description() {
        return "Draws text at coordinates. Args: x, y, text.";
    }

    private record Obs(boolean success, String message) implements FastObservation {}

    @Override
    public FastObservation execute(Map<String, Object> args) {
        try {
            int x = Integer.parseInt(String.valueOf(args.get("x")));
            int y = Integer.parseInt(String.valueOf(args.get("y")));
            String text = String.valueOf(args.get("text"));

            // FastTerminalScene has writeString
            context.getScene().writeString(x, y, text, context.getFg(), context.getBg());
            context.render();

            return new Obs(true, "Drew text '" + text + "' at " + x + "," + y);
        } catch (Exception e) {
            return new Obs(false, "Failed to draw text: " + e.getMessage());
        }
    }
}
