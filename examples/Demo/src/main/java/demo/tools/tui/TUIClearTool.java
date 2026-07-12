package demo.tools.tui;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.util.Map;

public class TUIClearTool implements FastTool {
    private final FastTUIContext context;

    public TUIClearTool(FastTUIContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "tui.clear";
    }

    public String description() {
        return "Clears the TUI scene.";
    }

    private record Obs(boolean success, String message) implements FastObservation {}

    @Override
    public FastObservation execute(Map<String, Object> args) {
        try {
            context.clear();
            context.render();
            return new Obs(true, "TUI scene cleared.");
        } catch (Exception e) {
            return new Obs(false, "Failed to clear TUI: " + e.getMessage());
        }
    }
}
