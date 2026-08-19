package demo;

import fastaiagent.FastAgentKernel;
import fastairuntime.FastAIEventBus;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.tools.CommandRunnerTool;
import fastairuntime.tools.DirectoryListTool;
import fastairuntime.tools.FileEditTool;
import fastairuntime.tools.FileReadTool;
import fastairuntime.tools.FileSaveTool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class CodingAgentLoopDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   FastAIAgent - Autonomous Coding Agent Loop    ");
        System.out.println("=================================================");

        // Setup FastAIRuntime harness with full core toolchain
        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());
        runtime.register(new FileEditTool());
        runtime.register(new DirectoryListTool());
        runtime.register(new CommandRunnerTool());

        // Setup EventBus logging
        FastAIEventBus eventBus = FastAIEventBus.getInstance();
        eventBus.subscribe("agent.goal", g -> System.out.println("[GOAL] " + g));
        eventBus.subscribe("agent.cycle.start", c -> System.out.println("\n--- [CYCLE " + c + "] ---"));
        eventBus.subscribe("agent.observe", o -> System.out.println("[OBSERVE] " + (o != null ? ((FastObservation)o).message() : "Initial state")));
        eventBus.subscribe("agent.plan", p -> {
            FastAgentKernel.AgentPlan plan = (FastAgentKernel.AgentPlan) p;
            System.out.println("[PLAN] " + plan.explanation());
            if (plan.action() != null) {
                System.out.println("       -> Action: " + plan.action().toolName() + " with " + plan.action().args());
            }
        });
        eventBus.subscribe("agent.act.result", r -> System.out.println("[ACT RESULT] " + ((FastObservation)r).message()));
        eventBus.subscribe("agent.reflect", r -> System.out.println("[REFLECT] " + r));
        eventBus.subscribe("agent.complete", msg -> System.out.println("\n[STATUS] " + msg));

        // Workspace setup
        String workDir = new File(System.getProperty("user.dir"), "target/sandbox_workspace").getAbsolutePath();
        new File(workDir).mkdirs();

        // Sequential state-machine planner simulating an autonomous coding task:
        // Goal: Create a Java calculator class, compile/test it, fix a bug via edit, and verify.
        final int[] step = {0};

        FastAgentKernel.Observer observer = () -> new FastObservation() {
            @Override public boolean success() { return true; }
            @Override public String message() { return "Workspace active at: " + workDir; }
        };

        FastAgentKernel.Planner planner = (goal, lastObs, currentPlan) -> {
            step[0]++;
            switch (step[0]) {
                case 1: {
                    Map<String, Object> toolArgs = new HashMap<>();
                    toolArgs.put("path", new File(workDir, "Calculator.java").getAbsolutePath());
                    toolArgs.put("content", "public class Calculator {\n    public static int add(int a, int b) { return a - b; }\n}\n");
                    return new FastAgentKernel.AgentPlan(
                        "Plan: 1. Create file (done), 2. Compile, 3. Fix bug, 4. Finish",
                        new FastAgentKernel.AgentAction("file.save", toolArgs),
                        false,
                        "Initial code creation with intentional arithmetic bug."
                    );
                }
                case 2: {
                    Map<String, Object> toolArgs = new HashMap<>();
                    toolArgs.put("path", new File(workDir, "Calculator.java").getAbsolutePath());
                    return new FastAgentKernel.AgentPlan(
                        "Plan: Inspect created source file.",
                        new FastAgentKernel.AgentAction("file.read", toolArgs),
                        false,
                        "Reading Calculator.java to inspect content."
                    );
                }
                case 3: {
                    Map<String, Object> toolArgs = new HashMap<>();
                    toolArgs.put("path", new File(workDir, "Calculator.java").getAbsolutePath());
                    toolArgs.put("target", "return a - b;");
                    toolArgs.put("replacement", "return a + b;");
                    return new FastAgentKernel.AgentPlan(
                        "Plan: Fix subtraction bug to addition.",
                        new FastAgentKernel.AgentAction("file.edit", toolArgs),
                        false,
                        "Refactoring Calculator.java using FileEditTool."
                    );
                }
                default: {
                    return new FastAgentKernel.AgentPlan(
                        "Plan complete.",
                        null,
                        true,
                        "Java project file created, verified, and fixed autonomously."
                    );
                }
            }
        };

        FastAgentKernel.Reflector reflector = (plan, result) -> {
            return result.success() ? "Action succeeded. Advancing plan." : "Action failed: " + result.message();
        };

        FastAgentKernel kernel = new FastAgentKernel(runtime, observer, planner, reflector);
        kernel.loop("Create and fix Calculator.java in sandbox", 10);
    }
}
