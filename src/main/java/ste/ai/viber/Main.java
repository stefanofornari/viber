package ste.ai.viber;

public class Main {
    public static void main(String[] args) {
        throw new UnsupportedOperationException(
            "Main requires a ChatModel implementation. " +
            "In tests, use DummyChatModel. In production, wire a real ChatModel " +
            "through a factory or dependency injection."
        );
    }
}
