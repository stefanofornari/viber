package ste.ai.viber.renderer;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.assertj.core.api.BDDAssertions.then;

class ToolInvocationPaneTest extends ApplicationTest {

    @Test
    void rawContent_preservesCommasInsideQuotedArguments() {
        String raw = "AskQuestion(question=\"Since we are moving from a basic calculator to one that handles complex expressions with parentheses and operator precedence, would you prefer the implementation to use a Third-Party expression evaluation library (like exp4j or JEP) to ensure robustness, or should we implement a custom recursive descent parser from scratch for educational/minimal dependency reasons?\")";
        ToolInvocationPane pane = new ToolInvocationPane(raw);
        
        then(pane.toolName()).isEqualTo("AskQuestion");
        
        then(pane.argumentsPanel.getChildren()).hasSize(1);
        
        ArgumentChip chip = (ArgumentChip) pane.argumentsPanel.getChildren().get(0);
        then(chip.name()).isEqualTo("question");
        then(chip.value()).isEqualTo("Since we are moving from a basic calculator to one that handles complex expressions with parentheses and operator precedence, would you prefer the implementation to use a Third-Party expression evaluation library (like exp4j or JEP) to ensure robustness, or should we implement a custom recursive descent parser from scratch for educational/minimal dependency reasons?");
    }
}