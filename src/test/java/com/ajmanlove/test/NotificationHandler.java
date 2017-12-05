package com.ajmanlove.test;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

public class NotificationHandler implements HttpHandler {
    public LinkedBlockingQueue<Message> messages;
    private ObjectMapper mapper;

    public NotificationHandler() {
        this.messages = new LinkedBlockingQueue<>();
        this.mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        t.sendResponseHeaders(200, 0);
        InputStreamReader isr = new InputStreamReader(t.getRequestBody());
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        Message notification = mapper.readValue(line, Message.class);
        messages.add(notification);
    }

    public LinkedBlockingQueue<Message> getMessages() {
        return messages;
    }
}
