package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 9: Software Development Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Jira-Style Task-Management Workflow Agent.
 */
public final class JiraWorkflowAgentDemo {

    private static final Map<String, TaskInfo> BOARD = new LinkedHashMap<>();
    private static final Path JSON_PATH = Path.of("target/jira_board.json");

    public static void main(String[] args) {
        System.out.println("=== Running Jira-Style Task-Management Workflow Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // Setup Jira Runtime
        FastAIRuntime runtime = new FastAIRuntime();

        // 1. Create Task Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "jira.create_task"; }
            public String description() { return "id=<task_id>,desc=<task_description>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String id = String.valueOf(args.get("id")).trim().toUpperCase();
                String desc = String.valueOf(args.get("desc"));
                BOARD.put(id, new TaskInfo(id, desc, "TODO"));
                saveBoard();
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Created task " + id + " in status TODO."; }
                };
            }
        });

        // 2. Update Status Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "jira.update_status"; }
            public String description() { return "id=<task_id>,status=<TODO|IN_PROGRESS|DONE>"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String id = String.valueOf(args.get("id")).trim().toUpperCase();
                String status = String.valueOf(args.get("status")).trim().toUpperCase();
                if (!BOARD.containsKey(id)) {
                    return new FastObservation() {
                        @Override public boolean success() { return false; }
                        @Override public String message() { return "Task " + id + " not found."; }
                    };
                }
                BOARD.get(id).status = status;
                saveBoard();
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return "Updated task " + id + " to " + status + "."; }
                };
            }
        });

        // 3. List Tasks Tool
        runtime.register(new FastTool() {
            @Override public String name() { return "jira.list_tasks"; }
            public String description() { return "no arguments"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                StringBuilder sb = new StringBuilder("Jira Board:\n");
                if (BOARD.isEmpty()) {
                    sb.append("(Empty)");
                } else {
                    for (TaskInfo info : BOARD.values()) {
                        sb.append(String.format("- %s [%s]: %s\n", info.id, info.status, info.description));
                    }
                }
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return sb.toString(); }
                };
            }
        });

        String systemPrompt = FastAIPromptBuilder.buildSystemPrompt(runtime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot bot = new FastAIBot(brain, systemPrompt, out, out);
        FastAIAgent agent = new FastAIAgent(bot, runtime, new TokyoNightLogger());

        // Initial tasks seed
        BOARD.put("JIRA-101", new TaskInfo("JIRA-101", "Build DB Schema", "TODO"));
        BOARD.put("JIRA-102", new TaskInfo("JIRA-102", "Configure Auth Controller", "TODO"));
        saveBoard();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            System.out.println("\nJira Board commands (e.g., 'Create task JIRA-103 to test auth', 'Start JIRA-101', 'list tasks', type 'exit' to quit):");

            while (true) {
                System.out.print("\nCommand Input > ");
                String cmd = reader.readLine();
                if (cmd == null || cmd.trim().equalsIgnoreCase("exit")) {
                    break;
                }
                if (cmd.trim().isEmpty()) continue;

                agent.run(cmd);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void saveBoard() {
        try {
            Files.createDirectories(JSON_PATH.getParent());
            StringBuilder json = new StringBuilder("[\n");
            int i = 0;
            for (TaskInfo info : BOARD.values()) {
                json.append(String.format("  {\"id\":\"%s\", \"description\":\"%s\", \"status\":\"%s\"}", 
                        info.id, info.description.replace("\"", "\\\""), info.status));
                if (++i < BOARD.size()) json.append(",");
                json.append("\n");
            }
            json.append("]");
            Files.writeString(JSON_PATH, json.toString());
        } catch (Exception ignored) {}
    }

    private static class TaskInfo {
        String id;
        String description;
        String status;

        TaskInfo(String id, String description, String status) {
            this.id = id;
            this.description = description;
            this.status = status;
        }
    }
}
