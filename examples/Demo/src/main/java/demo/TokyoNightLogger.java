package demo;

import fastaiagent.AgentLogger;
import fastansi.FastANSI;
import fastemojis.FastEmojis;

import java.util.Map;

public final class TokyoNightLogger implements AgentLogger {

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

    private long last = System.currentTimeMillis();

    private String tick() {
        long now = System.currentTimeMillis();
        long delta = now - last;
        last = now;
        return String.format("[%5d ms] ", delta);
    }

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

    @Override
    public void onGoal(String goal) {
        last = System.currentTimeMillis();
        System.out.println("\n" + TN_BG_DARKER + TN_GOAL + FastEmojis.ROBOT + "  Goal: " + goal + " ".repeat(Math.max(0, 60 - goal.length())) + FastANSI.RESET);
        printNoiseLine();
    }

    @Override
    public void onThoughts(String thoughts) {
        printPanelFrame("Thought Trace", thoughts, TN_BG_SIDE, TN_THOUGHT);
    }

    @Override
    public void onCommand(String command) {
        System.out.println(tick() + TN_BG_HL + TN_CMD + FastEmojis.LIGHTNING + "  COMMAND: " + String.format("%-46s", command) + FastANSI.RESET);
    }

    @Override
    public void onActiveStep(String tool, Map<String, Object> args) {
        System.out.println(tick() + TN_BG_DARK + TN_STEP + FastEmojis.GEAR + "  " + String.format("%-55s", tool + " " + args) + FastANSI.RESET);
    }

    @Override
    public void onObservation(boolean success, String message) {
        if (success) {
            System.out.println(tick() + TN_BG_DARKER + TN_OBS + FastEmojis.CHECK + "  " + String.format("%-55s", message) + FastANSI.RESET);
        } else {
            System.out.println(tick() + TN_BG_ERROR + TN_FG_BLACK + FastEmojis.ERROR_RED + "  ERROR: " + String.format("%-49s", message) + FastANSI.RESET);
        }
        printNoiseLine();
    }
}
