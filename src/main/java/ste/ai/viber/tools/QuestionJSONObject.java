package ste.ai.viber.tools;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import ste.lazyjava.annotation.NonBlank;
import ste.lazyjava.annotation.NonNull;
import static ste.lloop.Loop.on;
import ste.llm.tools.InvocationJSONObject;

/**
 * Specialized InvocationJSONObject for Ask tools.
 * It has only one optional argument "options" which is a JSONArray of String values
 * representing the different choices of a multichoice question.
 */
public class QuestionJSONObject extends InvocationJSONObject {

    public QuestionJSONObject(@NonBlank final String name) {
        super(name);
    }

    public QuestionJSONObject(@NonBlank final String name, @NonNull final JSONObject arguments) {
        super(name, arguments);
    }

    public QuestionJSONObject(@NonBlank final String name, @NonNull final String question) {
        super(name, new JSONObject().put("question", question));
    }

    public QuestionJSONObject(@NonBlank final String name, @NonNull final String question, @NonNull final List<String> options) {
        super(name, new JSONObject()
            .put("question", question)
            .put("options", new JSONArray(options)));
    }

    /**
     * Returns the available choices from the "options" argument.
     * If "options" is not defined, returns an empty list.
     * If "options" is a JSONArray, returns the string elements.
     * If "options" is not a JSONArray, returns a list containing its string representation.
     */
    public List<String> choices() {
        if (!arguments.has("options")) {
            return List.of();
        }

        final Object optionsObj = arguments.get("options");
        if (!(optionsObj instanceof JSONArray array)) {
            return List.of(String.valueOf(optionsObj));
        }

        final List<String> choices = new ArrayList<>();
        on(array.toList()).loop((item) -> {
            if (item instanceof String str) {
                choices.add(str);
            }
        });
        return List.copyOf(choices);
    }

    /**
     * Returns the question text from the "question" argument.
     * Returns empty string if not present.
     */
    public String question() {
        return arguments.optString("question", "");
    }
}