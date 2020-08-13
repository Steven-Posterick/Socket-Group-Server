package dev.stevenposterick.data.message;

public enum MessageType {
    DISCONNECTED("$(DISCONNECTED)"),
    CONNECTED("$(CONNECTED)"),
    MESSAGE("$(MESSAGE)");

    private final String message;

    MessageType(String message) {
        this.message = message;
    }

    public String getMessageStart() {
        return message;
    }

}
