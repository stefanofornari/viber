package ste.ai.viber.tools;

import java.util.List;
import ste.lazyjava.annotation.NonBlank;
import ste.uzzo.agent.tools.AskTools;

/**
 * Specialized QuestionJSONObject for confirmation questions.
 * Hardcodes options to "Yes" and "No".
 */
public class ConfirmationJSONObject extends QuestionJSONObject {

    public ConfirmationJSONObject(@NonBlank final String question) {
        super(AskTools.ASK_CONFIRMATION, question, List.of("Yes", "No"));
    }

}