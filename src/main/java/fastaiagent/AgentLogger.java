package fastaiagent;

import java.util.Map;

public interface AgentLogger {
    void onGoal(String goal);
    void onThoughts(String thoughts);
    void onCommand(String command);
    void onActiveStep(String tool, Map<String, Object> args);
    void onObservation(boolean success, String message);
}
