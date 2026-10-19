package ste.ai.viber.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.agent.tool.Tool;
import ste.llm.tools.ToolPolicy;
import ste.llm.tools.ToolPolicy.Policy;
import ste.uzzo.agent.tools.AskTools;

public class ViberCLIAskTools implements AskTools {

    private final BufferedReader reader;

    public ViberCLIAskTools() {
        this(new BufferedReader(new InputStreamReader(System.in)));
    }

    ViberCLIAskTools(final BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    @Tool("Ask the user an open-ended question and return their response")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askQuestion(final String question) {
        System.out.println(question);
        System.out.flush();
        try {
            String line = reader.readLine();
            return line != null ? line : "";
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    @Tool("Ask the user to select one option from a list")
    @ToolPolicy(Policy.INTERACTIVE)
    public String askSingleChoice(final String question, final List<String> options) {
        System.out.println(question);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }
        System.out.print("Enter your choice (1-" + options.size() + "): ");
        System.out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                return options.isEmpty() ? "" : options.get(0);
            }
            int choice = Integer.parseInt(line.trim()) - 1;
            if (choice >= 0 && choice < options.size()) {
                return options.get(choice);
            }
            return options.isEmpty() ? "" : options.get(0);
        } catch (IOException | NumberFormatException e) {
            return options.isEmpty() ? "" : options.get(0);
        }
    }

    @Override
    @Tool("Ask the user to select multiple options from a list")
    @ToolPolicy(Policy.INTERACTIVE)
    public List<String> askMultipleChoice(final String question, final List<String> options) {
        System.out.println(question);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }
        System.out.print("Enter your choices (comma-separated, e.g., 1,3): ");
        System.out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                return List.of();
            }
            List<String> selected = new ArrayList<>();
            for (String part : line.split(",")) {
                try {
                    int choice = Integer.parseInt(part.trim()) - 1;
                    if (choice >= 0 && choice < options.size()) {
                        selected.add(options.get(choice));
                    }
                } catch (NumberFormatException e) {
                    // skip invalid entries
                }
            }
            return selected;
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    @Tool("Ask the user to confirm a decision (yes/no)")
    @ToolPolicy(Policy.INTERACTIVE)
    public boolean askConfirmation(final String question) {
        System.out.println(question + " [y/n]");
        System.out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                return false;
            }
            String trimmed = line.trim().toLowerCase();
            return trimmed.equals("y") || trimmed.equals("yes");
        } catch (IOException e) {
            return false;
        }
    }
}
