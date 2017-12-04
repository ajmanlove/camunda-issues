package com.ajmanlove.camunda.plugin.tasks;

import com.ajmanlove.camunda.plugin.CustomCamundaPlugin;
import com.ajmanlove.camunda.plugin.MessageHelper;
import com.ajmanlove.camunda.plugin.messages.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

public class UserTaskCreatedListener implements TaskListener {
    private static final Logger logger = LoggerFactory.getLogger(UserTaskCreatedListener.class);

    public UserTaskCreatedListener() {
    }

    public void notify(DelegateTask delegateTask) {
        logger.info("User task created {}, {}", delegateTask.getId(), delegateTask.getTaskDefinitionKey());

        String notifyUrl = (String) delegateTask.getExecution().getVariable("notifyUrl");
        Message msg = new Message(Message.Type.USER_TASK_CREATED, delegateTask.getTaskDefinitionKey());
        MessageHelper.sendMessage(notifyUrl, msg);
    }
}
