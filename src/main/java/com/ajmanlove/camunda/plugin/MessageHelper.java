package com.ajmanlove.camunda.plugin;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessageHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MessageHelper.class);

    public static void sendMessage(String sendUrl, Message message) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(sendUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String body = mapper.writeValueAsString(message);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

        } catch (Throwable e) {
            logger.error("Failed to POST message",e);
        } finally {
            if (conn!=null) {
                conn.disconnect();
            }
        }
    }
}
