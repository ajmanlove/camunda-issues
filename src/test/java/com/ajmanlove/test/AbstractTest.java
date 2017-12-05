package com.ajmanlove.test;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTest.class);

    // TODO unsure if this would work on linux or windows...
    // This is the host ip that a container can use to contact the host in docker for mac
    protected static final String CALLBACK_HOST = "localhost";


    protected List<LockedExternalTask> awaitExtTask(ExternalTaskService ets, String workerId, String topic, int timeout) throws Exception {
        return TestHelper.awaitExtTask(ets,workerId,topic,timeout);
    }

    protected List<Message> awaitMessages(NotificationHandler handler, int timeout, int expected) throws Exception {
        return TestHelper.awaitMessages(handler, timeout, expected);
    }


    protected Future startHandler(NotificationHandler handler, AtomicBoolean shutdown) throws Exception {
        return TestHelper.startHandler(handler, shutdown);
    }

}
