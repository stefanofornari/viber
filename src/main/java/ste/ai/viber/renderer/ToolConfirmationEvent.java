package ste.ai.viber.renderer;

import javafx.event.Event;
import javafx.event.EventType;

public class ToolConfirmationEvent extends Event {

    public static final EventType<ToolConfirmationEvent> TOOL_CONFIRMATION = new EventType<>(Event.ANY, "TOOL_CONFIRMATION");

    private final boolean accepted;

    public ToolConfirmationEvent(final boolean accepted) {
        super(TOOL_CONFIRMATION);
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
