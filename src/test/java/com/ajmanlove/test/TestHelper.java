package com.ajmanlove.test;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestHelper {
    private static final Logger logger = LoggerFactory.getLogger(TestHelper.class);

    public static List<LockedExternalTask> awaitExtTask(ExternalTaskService ets, String workerId, String topic, int timeout) throws Exception {
        List<LockedExternalTask> tasks = new ArrayList<>();
        int cntr = 0;
        while (tasks.size()==0) {
            if (cntr>= timeout) {
                throw new RuntimeException("Failed awaiting task of topic " + topic);
            }
            tasks = ets.fetchAndLock(1, workerId)
                    .topic(topic, 60L*1000)
                    .execute();
            if (tasks.size()==0) {
                Thread.sleep(1000);
                cntr++;
            }
        }
        return tasks;
    }

    public static List<Message> awaitMessages(NotificationHandler handler, int timeout, int expected) throws Exception {
        int cntr = 0;
        while (handler.messages.size()<expected) {
            if (cntr >= timeout) {
                throw new RuntimeException(String.format("Timed out waiting for %d messages", expected));
            }
            logger.info("Awaiting {} messages from event handlers, current : {}", expected, handler.messages.size());
            Thread.sleep(1000);
            cntr++;
        }

        logger.info("Received {} messages, expected {}", handler.messages.size(), expected);
        List<Message> ret = new ArrayList<>();
        handler.messages.drainTo(ret);
        return ret;
    }


    public static Future startHandler(NotificationHandler handler, AtomicBoolean shutdown) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0",8081), 0);
        ThreadPoolExecutor exec = getThreadPool();
        server.createContext("/test", handler);
        server.setExecutor(null); // creates a default executor
        return exec.submit(() -> {
            try {
                server.start();
                while(!shutdown.get()) {
                    Thread.sleep(5000);
                }
                server.stop(0);
            } catch (Exception e) {
                System.out.println("Caught an error");
            }
        });


    }

    public static ThreadPoolExecutor getThreadPool() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());
    }
}
