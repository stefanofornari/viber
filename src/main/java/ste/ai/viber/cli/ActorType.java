package ste.ai.viber.cli;

/**
 * The kinds of actor the main window can drive.
 */
enum ActorType {
    FX("FXActor"),
    LANGCHAIN4J("LangChain4jActor"),
    SCOPE("ScopeActor"),
    STORY("StoryActor"),
    CODING("CodingActor");

    private final String label;

    ActorType(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
