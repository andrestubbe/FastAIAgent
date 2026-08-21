package demo;

import fastai.AI;
import fastai.AIRequest;
import fastai.AIResponse;
import fastai.Usage;
import fastaiagent.FastAgentKernel;
import fastaireasoner.FastAIReasoner;
import fastaireasoner.ReasoningResult;
import fastairuntime.FastAIEventBus;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.tools.FileEditTool;
import fastairuntime.tools.FileReadTool;
import fastairuntime.tools.FileSaveTool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class SelfHealingReflectionDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   FastAIAgent + FastAIReasoner - Chain-of-Thought Self-Healing Loop      ");
        System.out.println("==========================================================================");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());
        runtime.register(new FileEditTool());

        FastAIEventBus eventBus = FastAIEventBus.getInstance();
        eventBus.subscribe("agent.goal", g -> System.out.println("\n[GOAL] " + g));
        eventBus.subscribe("agent.cycle.start", c -> System.out.println("\n--- [HEALING CYCLE " + c + "] ---"));
        eventBus.subscribe("agent.observe", o -> System.out.println("[OBSERVE] " + (o != null ? ((FastObservation)o).message() : "Initial state")));
        eventBus.subscribe("agent.plan", p -> {
            FastAgentKernel.AgentPlan plan = (FastAgentKernel.AgentPlan) p;
            System.out.println("[PLAN] " + plan.explanation());
            if (plan.action() != null) {
                System.out.println("       -> Action: " + plan.action().toolName() + " (" + plan.action().args() + ")");
            }
        });
        eventBus.subscribe("agent.act.result", r -> System.out.println("[ACT RESULT] " + ((FastObservation)r).message()));
        eventBus.subscribe("agent.reflect", r -> System.out.println("[COGNITIVE REFLECTION] " + r));
        eventBus.subscribe("agent.complete", msg -> System.out.println("\n[STATUS] " + msg));

        String workDir = new File(System.getProperty("user.dir"), "target/sandbox_healing").getAbsolutePath();
        new File(workDir).mkdirs();

        AI reasonerAI = new AI() {
            @Override
            public AIResponse generate(AIRequest request) {
                return new AIResponse(
                    "Step 1: Parse compiler stack trace -> incompatible types int to String\n" +
                    "Step 2: Root cause analysis -> Variable result was initialized as String instead of int\n" +
                    "Step 3: Solution -> Apply FileEditTool replacing 'String result' with 'int result'",
                    Usage.ZERO, 0.0
                );
            }
            @Override public void stream(String prompt, Consumer<String> tokenHandler) {}
            @Override public List<String> getModels() { return List.of("mock-healer"); }
        };

        FastAIReasoner reasoner = FastAIReasoner.chainOfThought(reasonerAI);

        final int[] step = {0};

        FastAgentKernel.Observer observer = () -> new FastObservation() {
            @Override public boolean success() { return true; }
            @Override public String message() { return "Workspace scanned: " + workDir; }
        };

        FastAgentKernel.Planner planner = (goal, lastObs, currentPlan) -> {
            step[0]++;
            if (step[0] == 1) {
                Map<String, Object> toolArgs = new HashMap<>();
                toolArgs.put("path", new File(workDir, "ParserBug.java").getAbsolutePath());
                toolArgs.put("content", "public class ParserBug {\n    public static int parse() {\n        String result = 42;\n        return result;\n    }\n}\n");
                return new FastAgentKernel.AgentPlan(
                    "Initial source authoring with intentional syntax/type error",
                    new FastAgentKernel.AgentAction("file.save", toolArgs),
                    false,
                    "Writing initial broken ParserBug.java"
                );
            }
            if (step[0] == 2) {
                // Self-healing patch generated after CoT reflection
                Map<String, Object> toolArgs = new HashMap<>();
                toolArgs.put("path", new File(workDir, "ParserBug.java").getAbsolutePath());
                toolArgs.put("target", "String result = 42;");
                toolArgs.put("replacement", "int result = 42;");
                return new FastAgentKernel.AgentPlan(
                    "Apply targeted source patch",
                    new FastAgentKernel.AgentAction("file.edit", toolArgs),
                    false,
                    "Self-healing patch applied using FileEditTool"
                );
            }
            return new FastAgentKernel.AgentPlan("Complete", null, true, "Source successfully healed and verified.");
        };

        FastAgentKernel.Reflector reflector = (plan, result) -> {
            if (step[0] == 1) {
                ReasoningResult diag = reasoner.reason("Compiler error: incompatible types java.lang.String and int");
                return "Chain-of-Thought Diagnosis:\n" + diag.bestPath();
            }
            return "Patch verified successfully.";
        };

        FastAgentKernel kernel = new FastAgentKernel(runtime, observer, planner, reflector);
        kernel.loop("Self-heal broken Java class", 5);
    }
}
