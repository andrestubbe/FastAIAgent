package demo;

import fastaiagent.FastAgentKernel;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;
import fastairuntime.tools.FileSaveTool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class CodingPlanActSubDemo {
    public static void main(String[] args) {
        System.out.println("=== Phase 2 & 3: Plan & Act Sub-Demo ===");
        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileSaveTool());

        String workDir = new File(System.getProperty("user.dir"), "target/sandbox_workspace").getAbsolutePath();
        
        // Planning
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("path", new File(workDir, "PlanActTest.java").getAbsolutePath());
        argsMap.put("content", "public class PlanActTest {}\n");
        
        FastAgentKernel.AgentPlan plan = new FastAgentKernel.AgentPlan(
            "Create PlanActTest.java",
            new FastAgentKernel.AgentAction("file.save", argsMap),
            false,
            "Direct action generated from plan."
        );
        System.out.println("[PLAN] " + plan.explanation() + " -> Tool: " + plan.action().toolName());

        // Acting
        FastCommand cmd = new FastCommand(plan.action().toolName(), plan.action().args());
        FastObservation result = runtime.execute(cmd);
        System.out.println("[ACT RESULT] " + result.message());
    }
}
