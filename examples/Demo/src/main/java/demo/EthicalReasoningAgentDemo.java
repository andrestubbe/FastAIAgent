package demo;

import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaiagent.FastAIPromptBuilder;
import fastaibot.FastAIBot;
import fastairuntime.FastAIRuntime;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastairuntime.tools.FileSaveTool;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Demo referencing Chapter 12: Ethical and Explainable Agents
 * Source Concept: "30 Agents Every AI Engineer Must Build" (Packt Publishing)
 * Feature: Ethical Reasoning Agent (Fair HR Recruiting Assistant).
 */
public final class EthicalReasoningAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== Running Ethical Reasoning Agent Demo ===");

        AI brain = FastAI.connect("ollama:qwen2.5:3b");
        Consumer<String> out = token -> {};

        // 1. Step 1 Runtime (Candidate extraction only)
        FastAIRuntime hrRuntime = new FastAIRuntime();
        hrRuntime.register(new FastTool() {
            @Override public String name() { return "hr.get_candidates"; }
            @Override public String description() { return "no arguments"; }
            @Override public FastObservation execute(Map<String, Object> args) {
                String candidates = "Candidates:\n" +
                        "1. Anna (Gender: Female, Age: 23) - 1 year Python experience.\n" +
                        "2. Bob (Gender: Male, Age: 58) - 15 years Java, Spring Boot experience.\n" +
                        "3. Charlie (Gender: Male, Age: 21) - No technical skills, very enthusiastic.";
                return new FastObservation() {
                    @Override public boolean success() { return true; }
                    @Override public String message() { return candidates; }
                };
            }
        });

        String hrSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(hrRuntime) + 
                "\nCRITICAL RULE: You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot hrBot = new FastAIBot(brain, hrSystemPrompt, out, out);
        FastAIAgent hrAgent = new FastAIAgent(hrBot, hrRuntime, new TokyoNightLogger());

        // Step 1: Fetch Candidates
        System.out.println("\n--- STEP 1: FETCHING CANDIDATE PROFILES ---");
        hrAgent.run("Retrieve candidate database.");

        String candidateList = hrBot.getHistory().messages().getLast().text();
        if (candidateList.contains("Tool Execution Result (hr.get_candidates): ")) {
            candidateList = candidateList.replace("Tool Execution Result (hr.get_candidates): ", "").trim();
        }

        // 2. Step 2 Runtime (Save Shortlist only)
        FastAIRuntime saveRuntime = new FastAIRuntime();
        saveRuntime.register(new FileSaveTool());

        // We explicitly tell the prompt builder that this agent is bound by strict ethical principles
        String ethicalSystemPrompt = FastAIPromptBuilder.buildSystemPrompt(saveRuntime) + 
                "\nCRITICAL ETHICAL RULES:\n" +
                "- Evaluate candidates strictly based on technical qualifications and experience.\n" +
                "- Do NOT consider protected attributes (Gender, Age).\n" +
                "- Do NOT select candidates with zero relevant skills.\n" +
                "- Write a fair, bias-free recommendation shortlist.\n" +
                "You must respond ONLY with the raw tool call. Do not add any thoughts, explanations, markdown, or greetings.";
        FastAIBot saveBot = new FastAIBot(brain, ethicalSystemPrompt, out, out);
        FastAIAgent saveAgent = new FastAIAgent(saveBot, saveRuntime, new TokyoNightLogger());

        // Run Step 2: Ethical Filtering and Shortlisting
        System.out.println("\n--- STEP 2: ETHICAL SHORTLIST FILTERING ---");
        saveAgent.run(String.format(
            "file.save|path=target/shortlist.txt,content=Apply ethical filtering. Identify qualified candidates from this list, ignoring age and gender, and save the result:\n%s",
            candidateList
        ));
    }
}
