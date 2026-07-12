package demo.tools.tui;

import fastterminal.FastTerminalScene;
import fastterminal.FastTerminalRenderer;
import fasttui.component.Panel;

import java.util.ArrayList;
import java.util.List;

public class FastTUIContext {
    private final FastTerminalScene scene;
    private final FastTerminalRenderer renderer;
    private int currentFg = 0xc0caf5; // TN_FG
    private int currentBg = 0x1f2335; // TN_BG_SIDE

    public FastTUIContext(int width, int height) {
        this.scene = new FastTerminalScene(0, 0, width, height);
        this.scene.clear();
        this.renderer = new FastTerminalRenderer(width, height);
        this.renderer.addScene(this.scene);
    }

    public FastTerminalScene getScene() {
        return scene;
    }

    public void setColor(int fg, int bg) {
        this.currentFg = fg;
        this.currentBg = bg;
    }

    public int getFg() { return currentFg; }
    public int getBg() { return currentBg; }

    public void clear() {
        scene.clear();
        for (int y = 0; y < scene.getHeight(); y++) {
            for (int x = 0; x < scene.getWidth(); x++) {
                scene.writeCell(x, y, ' ', currentFg, currentBg);
            }
        }
    }

    public void render() {
        try {
            // Access private arrays from FastTerminalScene
            java.lang.reflect.Field cbField = FastTerminalScene.class.getDeclaredField("codepointBuffer");
            java.lang.reflect.Field fgField = FastTerminalScene.class.getDeclaredField("fgBuffer");
            java.lang.reflect.Field bgField = FastTerminalScene.class.getDeclaredField("bgBuffer");
            java.lang.reflect.Field widthField = FastTerminalScene.class.getDeclaredField("width");
            java.lang.reflect.Field heightField = FastTerminalScene.class.getDeclaredField("height");
            
            cbField.setAccessible(true); fgField.setAccessible(true); bgField.setAccessible(true);
            widthField.setAccessible(true); heightField.setAccessible(true);
            
            int[] cb = (int[]) cbField.get(scene);
            int[] fg = (int[]) fgField.get(scene);
            int[] bg = (int[]) bgField.get(scene);
            int width = (int) widthField.get(scene);
            int height = (int) heightField.get(scene);
            
            StringBuilder sb = new StringBuilder();
            sb.append("\n"); // Clear separation
            
            int lastFg = -2;
            int lastBg = -2;
            boolean inAnsi = false;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = y * width + x;
                    int cp = cb[i];
                    int f = fg[i];
                    int b = bg[i];
                    
                    if (cp == -99) continue; // Skip continuation cell
                    
                    if (f != lastFg || b != lastBg) {
                        if (f == -2 && b == -2) {
                            sb.append("\033[0m");
                            inAnsi = false;
                        } else {
                            if (!inAnsi) { sb.append("\033[0m"); }
                            // FG
                            if (f != -2) sb.append("\033[38;2;").append((f >> 16) & 0xFF).append(";").append((f >> 8) & 0xFF).append(";").append(f & 0xFF).append("m");
                            // BG
                            if (b != -2) sb.append("\033[48;2;").append((b >> 16) & 0xFF).append(";").append((b >> 8) & 0xFF).append(";").append(b & 0xFF).append("m");
                            inAnsi = true;
                        }
                        lastFg = f;
                        lastBg = b;
                    }
                    
                    if (cp == 0 || !Character.isValidCodePoint(cp)) {
                        sb.append(" ");
                    } else {
                        sb.appendCodePoint(cp);
                    }
                }
                sb.append("\033[0m\n"); // Reset at end of line
                lastFg = -2;
                lastBg = -2;
            }
            
            System.out.println(sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
