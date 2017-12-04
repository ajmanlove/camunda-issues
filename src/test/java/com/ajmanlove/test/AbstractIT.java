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

public abstract class AbstractIT {
    private static final Logger logger = LoggerFactory.getLogger(AbstractIT.class);

    // TODO unsure if this would work on linux or windows...
    // This is the host ip that a container can use to contact the host in docker for mac
    protected static final String CALLBACK_HOST = "192.168.65.1";

    @BeforeClass
    public static void setup() throws Exception {

        int cntr = 0;
        int timeout = 30;

        Client client = ClientBuilder.newClient();
        while (true) {

            if (cntr >= timeout) {
                throw new RuntimeException("Timed out waiting for docker setup");
            }


            try {

                // Issue with getting response code from camunda rest...Unexpected end of file
                client.target("http://localhost:8080/engine-rest/engine")
                        .request(MediaType.APPLICATION_JSON)
                        .get();

                break;

            } catch (Throwable e) {
                logger.info("Awaiting integration system setup");
                cntr ++;
                Thread.sleep(1000);
            }

        }
    }

    protected List<LockedExternalTask> awaitExtTask(ExternalTaskService ets, String workerId, String topic, int timeout) throws Exception {
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

    protected List<Message> awaitMessages(NotificationHandler handler, int timeout, int expected) throws Exception {
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


    protected Future startHandler(NotificationHandler handler, AtomicBoolean shutdown) throws Exception {
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

    protected ThreadPoolExecutor getThreadPool() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());
    }

    protected class NotificationHandler implements HttpHandler {
        private LinkedBlockingQueue<Message> messages;
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

}
