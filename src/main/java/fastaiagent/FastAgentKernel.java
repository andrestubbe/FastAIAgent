package fastaiagent;

import fastairuntime.FastAIEventBus;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastCommand;
import fastairuntime.FastObservation;

import java.util.Map;
import java.util.function.Function;

public final class FastAgentKernel {

    public interface Observer {
        FastObservation observe();
    }

    public interface Planner {
        AgentPlan plan(String goal, FastObservation lastObservation, String currentPlanState);
    }

    public interface Reflector {
        String reflect(AgentPlan plan, FastObservation executionResult);
    }

    public record AgentAction(String toolName, Map<String, Object> args) {}
    public record AgentPlan(String updatedPlan, AgentAction action, boolean isComplete, String explanation) {}

    private final FastAIRuntime runtime;
    private final Observer observer;
    private final Planner planner;
    private final Reflector reflector;
    private final FastAIEventBus eventBus = FastAIEventBus.getInstance();

    public FastAgentKernel(FastAIRuntime runtime, Observer observer, Planner planner, Reflector reflector) {
        this.runtime = runtime;
        this.observer = observer;
        this.planner = planner;
        this.reflector = reflector;
    }

    public void loop(String goal, int maxCycles) {
        eventBus.emit("agent.goal", goal);
        String planState = "Goal: " + goal;
        FastObservation lastObs = null;

        int cycle = 0;
        while (cycle < maxCycles) {
            cycle++;
            eventBus.emit("agent.cycle.start", cycle);

            // 1. Observe
            lastObs = (observer != null) ? observer.observe() : null;
            eventBus.emit("agent.observe", lastObs);

            // 2. Plan
            AgentPlan plan = planner.plan(goal, lastObs, planState);
            planState = plan.updatedPlan();
            eventBus.emit("agent.plan", plan);

            if (plan.isComplete() || plan.action() == null) {
                eventBus.emit("agent.complete", "Goal accomplished or stopped.");
                break;
            }

            // 3. Act
            FastCommand cmd = new FastCommand(plan.action().toolName(), plan.action().args());
            FastObservation result = runtime.execute(cmd);
            eventBus.emit("agent.act.result", result);

            // 4. Reflect
            String reflection = (reflector != null) ? reflector.reflect(plan, result) : result.message();
            eventBus.emit("agent.reflect", reflection);

            lastObs = result;
        }
    }
}
