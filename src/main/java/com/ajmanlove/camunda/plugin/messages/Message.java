package com.ajmanlove.camunda.plugin.messages;

public class Message {

    public enum Type {
        USER_TASK_CREATED
    }

    private Type messageType;
    private String message;

    public Message() {}

    public Message(Type messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public Type getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }
}
