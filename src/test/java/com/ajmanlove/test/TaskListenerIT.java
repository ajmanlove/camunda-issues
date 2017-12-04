package com.ajmanlove.test;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.google.common.collect.ImmutableMap;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

public class TaskListenerIT extends AbstractIT {
    private static final Logger logger = LoggerFactory.getLogger(TaskListenerIT.class);

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"external_task_user_task_sync_start.bpmn"})
    @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
    /*
    Process model has synchronous start event and expects a message POSTED back upon TaskListener.EVENTNAME_CREATE
    of a User Task preceeded by an external task.
    Fails.
     */
    public void testTaskCreatedSyncStart() throws Exception {
        String workerId = "testTaskCreated";

        NotificationHandler handler = new NotificationHandler();
        AtomicBoolean shutdown = new AtomicBoolean(false);
        Future f = startHandler(handler, shutdown);

        try {
            ProcessEngineConfigurationImpl pec = processEngineRule.getProcessEngineConfiguration();

            RuntimeService rs = processEngineRule.getRuntimeService();
            ProcessInstance pi = rs.startProcessInstanceByKey(
                    "external_task_user_task_sync_start",
                    ImmutableMap.of("notifyUrl", String.format("http://%s:8081/test", CALLBACK_HOST))
            );

            ExternalTaskService ets = processEngineRule.getExternalTaskService();
            List<LockedExternalTask> tasks = awaitExtTask(ets,  workerId, "external_task",10);

            assertEquals(1, tasks.size());
            LockedExternalTask task = tasks.get(0);

            ets.complete(task.getId(), workerId);
            Task userTask = awaitTask("human_task_1", 15);

            List<Message> messages = awaitMessages(handler, 10,1);

            assertEquals(1, messages.size());
            Message msg = messages.get(0);
            assertEquals(Message.Type.USER_TASK_CREATED, msg.getMessageType());
            assertEquals(userTask.getTaskDefinitionKey(), msg.getMessage());
        } finally {
            shutdown.set(true);
            f.get();
        }


    }

    @Test
    @Deployment(resources = {"external_task_user_task_async_start.bpmn"})
    /*
    Process model has an asynchronous start event and expects a message POSTED back upon TaskListener.EVENTNAME_CREATE
    of a User Task preceeded by an external task.
    Fails.
     */
    public void testTaskCreatedASyncStart() throws Exception {
        String workerId = "testTaskCreatedASyncStart";

        NotificationHandler handler = new NotificationHandler();
        AtomicBoolean shutdown = new AtomicBoolean(false);
        Future f = startHandler(handler, shutdown);

        try {
            ProcessEngineConfigurationImpl pec = processEngineRule.getProcessEngineConfiguration();

            RuntimeService rs = processEngineRule.getRuntimeService();
            ProcessInstance pi = rs.startProcessInstanceByKey(
                    "external_task_user_task_async_start",
                    ImmutableMap.of("notifyUrl", String.format("http://%s:8081/test", CALLBACK_HOST))
            );

            ExternalTaskService ets = processEngineRule.getExternalTaskService();
            List<LockedExternalTask> tasks = awaitExtTask(ets,  workerId, "external_task",10);

            assertEquals(1, tasks.size());
            LockedExternalTask task = tasks.get(0);

            ets.complete(task.getId(), workerId);
            Task userTask = awaitTask("human_task_1", 15);

            List<Message> messages = awaitMessages(handler, 10,1);

            assertEquals(1, messages.size());
            Message msg = messages.get(0);
            assertEquals(Message.Type.USER_TASK_CREATED, msg.getMessageType());
            assertEquals(userTask.getTaskDefinitionKey(), msg.getMessage());
        } finally {
            shutdown.set(true);
            f.get();
        }

    }

    @Test
    @Deployment(resources = {"external_task_user_task_async_start_async_task.bpmn"})
    /*
    Process model has asynchronous start event and expects a message POSTED back upon TaskListener.EVENTNAME_CREATE
    of a User Task preceeded by an external task.
    Succeeds.
     */
    public void testTaskCreatedASyncStartAsyncTask() throws Exception {
        String workerId = "testTaskCreatedASyncStartAsyncTask";

        NotificationHandler handler = new NotificationHandler();
        AtomicBoolean shutdown = new AtomicBoolean(false);
        Future f = startHandler(handler, shutdown);

        try {
            ProcessEngineConfigurationImpl pec = processEngineRule.getProcessEngineConfiguration();

            RuntimeService rs = processEngineRule.getRuntimeService();
            ProcessInstance pi = rs.startProcessInstanceByKey(
                    "external_task_user_task_async_start_async_task",
                    ImmutableMap.of("notifyUrl", String.format("http://%s:8081/test", CALLBACK_HOST))
            );

            ExternalTaskService ets = processEngineRule.getExternalTaskService();
            List<LockedExternalTask> tasks = awaitExtTask(ets,  workerId, "external_task",10);

            assertEquals(1, tasks.size());
            LockedExternalTask task = tasks.get(0);

            ets.complete(task.getId(), workerId);
            Task userTask = awaitTask("human_task_1", 15);

            List<Message> messages = awaitMessages(handler, 10,1);

            assertEquals(1, messages.size());
            Message msg = messages.get(0);
            assertEquals(Message.Type.USER_TASK_CREATED, msg.getMessageType());
            assertEquals(userTask.getTaskDefinitionKey(), msg.getMessage());
        } finally {
            shutdown.set(true);
            f.get();
        }

    }

    private Task awaitTask(String key, int timeout) throws Exception {
        Task userTask = null;
        int cntr = 0;
        while (userTask==null) {
            if (cntr>= timeout) {
                throw new RuntimeException("Failed awaiting task of key " + key);
            }
            userTask = processEngineRule.getTaskService().createTaskQuery().taskDefinitionKey(key).singleResult();
            if (userTask==null) {
                Thread.sleep(1000);
                cntr++;
            }
        }
        return userTask;
    }

}
