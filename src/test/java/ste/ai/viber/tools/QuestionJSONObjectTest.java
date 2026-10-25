package ste.ai.viber.tools;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.jupiter.api.Test;

class QuestionJSONObjectTest {

    @Test
    void construction_with_name_only() {
        QuestionJSONObject o = new QuestionJSONObject("AskSingleChoice");

        then(o.name).isEqualTo("AskSingleChoice");
        then(o.arguments).isNotNull();
        then(o.choices()).isEmpty();
        then(o.question()).isEmpty();
    }

    @Test
    void construction_with_name_and_arguments() {
        JSONObject inner = new JSONObject("""
        {
            "question": "Choose a platform",
            "options": ["Web", "Mobile", "Desktop"]
        }
        """);
        QuestionJSONObject o = new QuestionJSONObject("AskSingleChoice", inner);

        then(o.name).isEqualTo("AskSingleChoice");
        then(o.arguments).isEqualTo(inner);
        then(o.question()).isEqualTo("Choose a platform");
        then(o.choices()).containsExactly("Web", "Mobile", "Desktop");
    }

    @Test
    void choices_returns_empty_when_options_not_present() {
        JSONObject inner = new JSONObject("""
        {
            "question": "What is your name?"
        }
        """);
        QuestionJSONObject o = new QuestionJSONObject("AskQuestion", inner);

        then(o.choices()).isEmpty();
    }

    @Test
    void choices_returns_string_when_options_is_not_array() {
        JSONObject inner = new JSONObject("""
        {
            "question": "Choose",
            "options": "not an array"
        }
        """);
        QuestionJSONObject o = new QuestionJSONObject("AskSingleChoice", inner);

        then(o.choices()).containsExactly("not an array");
    }

    @Test
    void choices_returns_empty_when_options_is_empty_array() {
        JSONObject inner = new JSONObject("""
        {
            "question": "Choose",
            "options": []
        }
        """);
        QuestionJSONObject o = new QuestionJSONObject("AskSingleChoice", inner);

        then(o.choices()).isEmpty();
    }

    @Test
    void choices_returns_only_string_values() {
        JSONObject inner = new JSONObject("""
        {
            "question": "Choose",
            "options": ["Web", 123, "Mobile", true, "Desktop", null]
        }
        """);
        QuestionJSONObject o = new QuestionJSONObject("AskSingleChoice", inner);

        then(o.choices()).containsExactly("Web", "Mobile", "Desktop");
    }

    @Test
    void question_returns_empty_when_not_present() {
        JSONObject inner = new JSONObject("""
        {
            "options": ["A", "B"]
        }
        """);
        QuestionJSONObject o = new QuestionJSONObject("AskSingleChoice", inner);

        then(o.question()).isEmpty();
    }

    @Test
    void throws_when_arguments_null() {
        thenThrownBy(() -> {
            new QuestionJSONObject("aTool", (JSONObject) null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throws_when_name_is_blank() {
        thenThrownBy(() -> {
            new QuestionJSONObject("", new JSONObject());
        }).isInstanceOf(IllegalArgumentException.class);

        thenThrownBy(() -> {
            new QuestionJSONObject("   ", new JSONObject());
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void record_components_accessible() {
        JSONObject inner = new JSONObject("{\"question\": \"test\", \"options\": [\"a\", \"b\"]}");
        QuestionJSONObject o = new QuestionJSONObject("toolName", inner);

        then(o.name).isEqualTo("toolName");
        then(o.arguments).isEqualTo(inner);
    }

    @Test
    void equality_and_hashcode() {
        JSONObject inner = new JSONObject("{\"question\": \"test\", \"options\": [\"a\", \"b\"]}");
        QuestionJSONObject o1 = new QuestionJSONObject("tool", inner);
        QuestionJSONObject o2 = new QuestionJSONObject("tool", inner);
        QuestionJSONObject o3 = new QuestionJSONObject("other", inner);

        then(o1).isEqualTo(o2);
        then(o1.hashCode()).isEqualTo(o2.hashCode());
        then(o1).isNotEqualTo(o3);
    }

    @Test
    void toString_contains_name_and_arguments() {
        JSONObject inner = new JSONObject("{\"question\": \"test\", \"options\": [\"a\", \"b\"]}");
        QuestionJSONObject o = new QuestionJSONObject("myTool", inner);

        String toString = o.toString();

        then(toString).contains("myTool");
        then(toString).contains("test");
        then(toString).contains("a");
        then(toString).contains("b");
    }
}