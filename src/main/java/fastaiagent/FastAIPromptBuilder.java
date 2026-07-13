package fastaiagent;

import fastairuntime.FastAIRuntime;
import fastairuntime.FastTool;

public class FastAIPromptBuilder {

    public static String buildSystemPrompt(FastAIRuntime runtime) {
        StringBuilder toolsDef = new StringBuilder();
        boolean hasSave = false, hasRead = false, hasList = false, hasRun = false, hasFetch = false, hasType = false, hasOpen = false, hasClose = false;
        
        for (FastTool tool : runtime.getRegisteredTools()) {
            String desc = tool.description();
            if (desc != null && !desc.isEmpty()) {
                toolsDef.append("- ").append(tool.name()).append(": ").append(desc).append("\n");
            } else {
                toolsDef.append("- ").append(tool.name()).append("\n");
            }
            if (tool.name().equals("file.save")) hasSave = true;
            if (tool.name().equals("file.read")) hasRead = true;
            if (tool.name().equals("dir.list")) hasList = true;
            if (tool.name().equals("os.run_command")) hasRun = true;
            if (tool.name().equals("browser.fetch")) hasFetch = true;
            if (tool.name().equals("keyboard.type")) hasType = true;
            if (tool.name().equals("windows.open_app")) hasOpen = true;
            if (tool.name().equals("windows.close_app")) hasClose = true;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a fast AI Agent. Convert user goals into a single structured tool call.\n");
        prompt.append("Available tools:\n").append(toolsDef);
        
        if (hasSave) prompt.append("For file saving, use: file.save|path=<file_path>,content=<text_to_save>\n");
        if (hasRead) prompt.append("For file reading, use: file.read|path=<file_path>\n");
        if (hasList) prompt.append("For directory listing, use: dir.list|path=<directory_path>\n");
        if (hasRun) prompt.append("For running shell commands, use: os.run_command|command=<cmd>\n");
        if (hasFetch) prompt.append("For fetching URLs, use: browser.fetch|url=<url>\n");
        if (hasType) prompt.append("For typing, use: keyboard.type|text=<text_to_type>\n");
        if (hasOpen) prompt.append("For opening apps, use: windows.open_app|path=<executable_path>\n");
        if (hasClose) prompt.append("For closing apps, use: windows.close_app|process_name=<process_name>\n");

        prompt.append("Always answer with the precise tool call in plain text (do NOT wrap it in markdown code blocks). Give no explanation.\n");
        prompt.append("Output format: tool_name|arg_key=arg_value. Example:\n");
        if (hasSave) {
            prompt.append("file.save|path=target/reasoning_output.txt,content=Executed successfully.");
        } else {
            prompt.append("tool_name|arg_key=arg_value");
        }

        return prompt.toString();
    }

}
