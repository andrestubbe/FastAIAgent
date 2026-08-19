package demo;

import fastaiagent.FastAgentKernel;
import fastairuntime.FastObservation;

import java.io.File;

public final class CodingObserveSubDemo {
    public static void main(String[] args) {
        System.out.println("=== Phase 1: Observation Sub-Demo ===");
        String workDir = new File(System.getProperty("user.dir"), "target/sandbox_workspace").getAbsolutePath();
        
        FastAgentKernel.Observer observer = () -> new FastObservation() {
            @Override public boolean success() { return true; }
            @Override public String message() { 
                return "Environment scanned: Workspace active at " + workDir + ", OS: " + System.getProperty("os.name");
            }
        };

        FastObservation obs = observer.observe();
        System.out.println("[OBSERVATION RESULT] " + obs.message());
    }
}
