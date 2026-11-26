package ste.ai.viber.storyeditor;

/**
 * Thrown when a text cannot be parsed as a uzz user story markdown document.
 */
public class UserStoryParseException extends IllegalArgumentException {

    /**
     * @param message a description of what is wrong with the markdown
     */
    public UserStoryParseException(final String message) {
        super(message);
    }
}
