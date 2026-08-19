package demo;

import fastaiagent.FastAgentKernel;
import fastairuntime.FastObservation;

import java.util.HashMap;

public final class CodingReflectSubDemo {
    public static void main(String[] args) {
        System.out.println("=== Phase 4: Reflection Sub-Demo ===");

        FastAgentKernel.AgentPlan plan = new FastAgentKernel.AgentPlan(
            "Execute edit",
            new FastAgentKernel.AgentAction("file.edit", new HashMap<>()),
            false,
            "Refactoring target code."
        );

        FastObservation failedResult = new FastObservation() {
            @Override public boolean success() { return false; }
            @Override public String message() { return "Target string not found."; }
        };

        FastAgentKernel.Reflector reflector = (p, r) -> {
            if (!r.success()) {
                return "CRITICAL: Plan failed during " + p.action().toolName() + " (" + r.message() + "). Re-planning required.";
            }
            return "SUCCESS: Step verified.";
        };

        String reflection = reflector.reflect(plan, failedResult);
        System.out.println("[REFLECTION LOGIC] " + reflection);
    }
}
