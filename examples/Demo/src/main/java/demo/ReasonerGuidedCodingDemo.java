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
import fastairuntime.tools.CommandRunnerTool;
import fastairuntime.tools.FileEditTool;
import fastairuntime.tools.FileReadTool;
import fastairuntime.tools.FileSaveTool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ReasonerGuidedCodingDemo {

    public static void main(String[] args) {
        System.out.println("===================================================================");
        System.out.println("   FastAIAgent + FastAIReasoner - Tree-of-Thoughts Guided Coding   ");
        System.out.println("===================================================================");

        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());
        runtime.register(new FileEditTool());
        runtime.register(new CommandRunnerTool());

        FastAIEventBus eventBus = FastAIEventBus.getInstance();
        eventBus.subscribe("agent.goal", g -> System.out.println("\n[GOAL] " + g));
        eventBus.subscribe("agent.cycle.start", c -> System.out.println("\n--- [REASONING CYCLE " + c + "] ---"));
        eventBus.subscribe("agent.observe", o -> System.out.println("[OBSERVE] " + (o != null ? ((FastObservation)o).message() : "Initial state")));
        eventBus.subscribe("agent.plan", p -> {
            FastAgentKernel.AgentPlan plan = (FastAgentKernel.AgentPlan) p;
            System.out.println("[SELECTED PLAN PATH] " + plan.explanation());
            if (plan.action() != null) {
                System.out.println("                     -> Action: " + plan.action().toolName() + " (" + plan.action().args() + ")");
            }
        });
        eventBus.subscribe("agent.act.result", r -> System.out.println("[ACT RESULT] " + ((FastObservation)r).message()));
        eventBus.subscribe("agent.reflect", r -> System.out.println("[REFLECT] " + r));
        eventBus.subscribe("agent.complete", msg -> System.out.println("\n[STATUS] " + msg));

        String workDir = new File(System.getProperty("user.dir"), "target/sandbox_reasoner").getAbsolutePath();
        new File(workDir).mkdirs();

        // High-speed simulated AI for Tree-of-Thoughts reasoning
        AI reasoningAI = new AI() {
            @Override
            public AIResponse generate(AIRequest request) {
                String sys = request.systemPrompt;
                if (sys != null && sys.contains("strategic solution explorer")) {
                    return new AIResponse(
                        "Candidate 1: Naive synchronized map\n" +
                        "Candidate 2: ReadWriteLock around HashMap\n" +
                        "Candidate 3: ConcurrentHashMap with computeIfAbsent",
                        Usage.ZERO, 0.0
                    );
                }
                if (sys != null && sys.contains("evaluation judge")) {
                    return new AIResponse(
                        "Candidate 3 (ConcurrentHashMap) selected: Lock-free reads, striped segment writes, optimal throughput.",
                        Usage.ZERO, 0.0
                    );
                }
                return new AIResponse("public class FastCache {\n    private final java.util.concurrent.ConcurrentHashMap<String, String> map = new java.util.concurrent.ConcurrentHashMap<>();\n}\n", Usage.ZERO, 0.0);
            }

            @Override public void stream(String prompt, Consumer<String> tokenHandler) {}
            @Override public List<String> getModels() { return List.of("mock-reasoner"); }
        };

        FastAIReasoner reasoner = FastAIReasoner.treeOfThoughts(reasoningAI, 3, 2);

        final int[] step = {0};

        FastAgentKernel.Observer observer = () -> new FastObservation() {
            @Override public boolean success() { return true; }
            @Override public String message() { return "Workspace scanned: " + workDir; }
        };

        FastAgentKernel.Planner planner = (goal, lastObs, currentPlan) -> {
            step[0]++;
            if (step[0] == 1) {
                System.out.println("[REASONER] Evaluating architectural alternatives via Tree-of-Thoughts...");
                ReasoningResult eval = reasoner.reason(goal);
                System.out.println("[REASONER] Explored " + eval.alternativeBranches().size() + " branches:");
                eval.alternativeBranches().forEach(b -> System.out.println("           * " + b));
                System.out.println("[REASONER] Decision Score: " + eval.confidenceScore() + " -> " + eval.bestPath());

                Map<String, Object> toolArgs = new HashMap<>();
                toolArgs.put("path", new File(workDir, "FastCache.java").getAbsolutePath());
                toolArgs.put("content", "package sandbox;\n\npublic class FastCache {\n    private final java.util.concurrent.ConcurrentHashMap<String, String> map = new java.util.concurrent.ConcurrentHashMap<>();\n}\n");

                return new FastAgentKernel.AgentPlan(
                    "Plan: 1. Author FastCache based on Candidate 3",
                    new FastAgentKernel.AgentAction("file.save", toolArgs),
                    false,
                    "Generated architectural implementation based on evaluated ToT decision."
                );
            }
            return new FastAgentKernel.AgentPlan("Complete", null, true, "Architecture verified and authored successfully.");
        };

        FastAgentKernel.Reflector reflector = (plan, result) -> result.success() ? "Action verified by reasoner." : "Action failed: " + result.message();

        FastAgentKernel kernel = new FastAgentKernel(runtime, observer, planner, reflector);
        kernel.loop("Design high-throughput thread-safe Cache class", 5);
    }
}
