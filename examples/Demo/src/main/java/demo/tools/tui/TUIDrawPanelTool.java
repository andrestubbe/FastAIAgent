package demo.tools.tui;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fasttui.component.Window;

import java.util.Map;

public class TUIDrawPanelTool implements FastTool {
    private final FastTUIContext context;
    private int nextY = 2; // Auto-layout cursor

    public TUIDrawPanelTool(FastTUIContext context) {
        this.context = context;
    }

    @Override
    public String name() {
        return "tui.draw_panel";
    }

    public String description() {
        return "Draws a panel with auto-layout. Args: title, content.";
    }

    private record Obs(boolean success, String message) implements FastObservation {}

    @Override
    public FastObservation execute(Map<String, Object> args) {
        try {
            String title = args.containsKey("title") ? String.valueOf(args.get("title")) : (args.containsKey("label") ? String.valueOf(args.get("label")) : "Panel");
            String content = args.containsKey("content") ? String.valueOf(args.get("content")) : (args.containsKey("text") ? String.valueOf(args.get("text")) : "");

            String[] lines = content.split("\n");
            int height = lines.length + 4; // padding

            Window p = new Window(2, nextY, 76, height, context.getBg());
            p.setTitle(title);
            p.setHasHeaderBar(true);
            p.setForegroundColor(context.getFg());
            p.setHeaderBg(context.getBg());
            p.setHeaderFg(context.getFg());
            p.setShowWindowButtons(false);
            p.setHasResizeButton(false);
            p.setHasShadow(false);
            
            p.render(context.getScene());

            int lineY = nextY + 2;
            for (String line : lines) {
                context.getScene().writeString(4, lineY, line, context.getFg(), context.getBg());
                lineY++;
            }

            nextY += height + 1;

            context.render();

            return new Obs(true, "Drew panel '" + title + "'");
        } catch (Exception e) {
            return new Obs(false, "Failed to draw panel: " + e.getMessage());
        }
    }
}
