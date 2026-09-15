package ste.ai.viber.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputTool implements ste.ai.viber.tools.Tool {

    @Tool("Prompts the user for input and returns the entered value")
    public String input(@P("The prompt to display to the user") String prompt) {
        System.out.println(prompt);
        System.out.flush();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();
            return line != null ? line : "(no name provided)";
        } catch (IOException e) {
            return "(no name provided)";
        }
    }
}
