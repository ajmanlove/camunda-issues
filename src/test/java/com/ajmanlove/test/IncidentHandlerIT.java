package com.ajmanlove.test;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.google.common.collect.ImmutableMap;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class IncidentHandlerIT extends AbstractIT {
    private static final Logger logger = LoggerFactory.getLogger(TaskListenerIT.class);

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda-integration.cfg.xml");

    @Test
    @Deployment(resources = {"external_task_incident.bpmn"})
    public void testExternalTaskIncident() throws Exception {
        String workerId = "testExternalTaskIncident";

        NotificationHandler handler = new NotificationHandler();
        AtomicBoolean shutdown = new AtomicBoolean(false);
        Future f = startHandler(handler, shutdown);

        try {
            ProcessEngineConfigurationImpl pec = processEngineRule.getProcessEngineConfiguration();

            RuntimeService rs = processEngineRule.getRuntimeService();
            ProcessInstance pi = rs.startProcessInstanceByKey(
                    "external_task_incident",
                    ImmutableMap.of("notifyUrl", String.format("http://%s:8081/test", CALLBACK_HOST))
            );

            ExternalTaskService ets = processEngineRule.getExternalTaskService();
            List<LockedExternalTask> tasks = awaitExtTask(ets,  workerId, "external_task",10);

            assertEquals(1, tasks.size());
            LockedExternalTask task = tasks.get(0);

            ets.handleFailure(task.getId(), workerId, "fooMessage", 0, 0L);

            List<Message> messages = awaitMessages(handler, 10,1);

            assertEquals(1, messages.size());
            Message msg = messages.get(0);
            assertEquals(Message.Type.INCIDENT_CREATED, msg.getMessageType());
            assertEquals("fooMessage", msg.getMessage());
        } finally {
            shutdown.set(true);
            f.get();
        }
    }

    @Test
    @Deployment(resources = {"job_incident.bpmn"})
    public void testJobIncident() throws Exception {
        String workerId = "testJobIncident";

        NotificationHandler handler = new NotificationHandler();
        AtomicBoolean shutdown = new AtomicBoolean(false);
        Future f = startHandler(handler, shutdown);

        try {
            ProcessEngineConfigurationImpl pec = processEngineRule.getProcessEngineConfiguration();

            RuntimeService rs = processEngineRule.getRuntimeService();
            ProcessInstance pi = rs.startProcessInstanceByKey(
                    "job_incident",
                    ImmutableMap.of("notifyUrl", String.format("http://%s:8081/test", CALLBACK_HOST))
            );

            ExternalTaskService ets = processEngineRule.getExternalTaskService();
            List<Message> messages = awaitMessages(handler, 10,1);

            assertEquals(1, messages.size());
            Message msg = messages.get(0);
            assertEquals(Message.Type.INCIDENT_CREATED, msg.getMessageType());
            assertThat(msg.getMessage(), containsString("Creating Foo Incident"));
            assertEquals("fooMessage", msg.getMessage());
        } finally {
            shutdown.set(true);
            f.get();
        }
    }
}
