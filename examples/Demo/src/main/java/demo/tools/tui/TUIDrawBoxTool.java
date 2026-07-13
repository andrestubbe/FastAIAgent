package demo.tools.tui;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fasttui.component.Window;

import java.util.Map;

public class TUIDrawBoxTool implements FastTool {
    private final FastTUIContext context;

    public TUIDrawBoxTool(FastTUIContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "tui.draw_box";
    }

    public String description() {
        return "Draws a TUI panel/box. Args: x, y, width, height, title (optional).";
    }

    private record Obs(boolean success, String message) implements FastObservation {}

    @Override
    public FastObservation execute(Map<String, Object> args) {
        try {
            int x = args.containsKey("x") ? Integer.parseInt(String.valueOf(args.get("x"))) : 2;
            int y = args.containsKey("y") ? Integer.parseInt(String.valueOf(args.get("y"))) : 12;
            int w = args.containsKey("width") ? Integer.parseInt(String.valueOf(args.get("width"))) : 76;
            int h = args.containsKey("height") ? Integer.parseInt(String.valueOf(args.get("height"))) : 4;
            String title = args.containsKey("title") ? String.valueOf(args.get("title")) : (args.containsKey("label") ? String.valueOf(args.get("label")) : null);

            Window p = new Window(x, y, w, h, context.getBg());
            if (title != null) {
                p.setTitle(title);
                p.setHasHeaderBar(true);
            }
            p.setForegroundColor(context.getFg());
            p.setHeaderBg(context.getBg());
            p.setHeaderFg(context.getFg());
            p.setShowWindowButtons(false);
            p.setHasResizeButton(false);
            p.setHasShadow(false);
            
            p.render(context.getScene());
            context.render();

            return new Obs(true, "Drew box '" + title + "' at " + x + "," + y);
        } catch (Exception e) {
            return new Obs(false, "Failed to draw box: " + e.getMessage());
        }
    }
}
