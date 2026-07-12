package fastaiagent;

import fastairuntime.FastAIRuntime;
import fastairuntime.FastTool;

public class FastAIPromptBuilder {

    public static String buildSystemPrompt(FastAIRuntime runtime) {
        StringBuilder toolsDef = new StringBuilder();
        for (FastTool tool : runtime.getRegisteredTools()) {
            toolsDef.append("- ").append(tool.name()).append("\n");
        }

        return "You are a fast AI Agent. Convert user goals into a single structured tool call.\n" +
                "Available tools:\n" +
                toolsDef.toString() +
                "For file saving, use: file.save|path=<file_path>,content=<text_to_save>\n" +
                "For file reading, use: file.read|path=<file_path>\n" +
                "For directory listing, use: dir.list|path=<directory_path>\n" +
                "For running shell commands, use: os.run_command|command=<cmd>\n" +
                "For fetching URLs, use: browser.fetch|url=<url>\n" +
                "For typing, use: keyboard.type|text=<text_to_type>\n" +
                "For opening apps, use: windows.open_app|path=<executable_path>\n" +
                "For closing apps, use: windows.close_app|process_name=<process_name>\n" +
                "Always answer with the precise tool call in plain text (do NOT wrap it in markdown code blocks). Give no explanation.\n" +
                "Output format: tool_name|arg_key=arg_value. Example:\n" +
                "file.save|path=target/reasoning_output.txt,content=Executed successfully.";
    }

}
