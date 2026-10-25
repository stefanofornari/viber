package ste.ai.viber.tools;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.jupiter.api.Test;

class ConfirmationJSONObjectTest {

    @Test
    void construction_with_question_only() {
        ConfirmationJSONObject o = new ConfirmationJSONObject("Do you agree?");

        then(o.name).isEqualTo("AskConfirmation");
        then(o.arguments).isNotNull();
        then(o.question()).isEqualTo("Do you agree?");
        then(o.choices()).containsExactly("Yes", "No");
    }

    @Test
    void choices_always_returns_yes_no() {
        ConfirmationJSONObject o = new ConfirmationJSONObject("test");

        then(o.choices()).containsExactly("Yes", "No");
    }

    @Test
    void throws_when_question_null() {
        thenThrownBy(() -> {
            new ConfirmationJSONObject((String) null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void options_are_set_in_arguments() {
        ConfirmationJSONObject o = new ConfirmationJSONObject("test");

        then(o.arguments.has("options")).isTrue();
        then(o.arguments.get("options")).isInstanceOf(org.json.JSONArray.class);
        then(o.arguments.getJSONArray("options").toList()).containsExactly("Yes", "No");
    }
}