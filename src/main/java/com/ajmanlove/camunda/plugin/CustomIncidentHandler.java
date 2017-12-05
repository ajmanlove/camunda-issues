package com.ajmanlove.camunda.plugin;

import com.ajmanlove.camunda.plugin.messages.Message;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomIncidentHandler implements IncidentHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomIncidentHandler.class);

    private final String type;
    private final IncidentHandler defaultHandler;
    private final RuntimeService runtimeService;

    public CustomIncidentHandler(IncidentHandler defaultHandler, String type, RuntimeService runtimeService) {
        this.defaultHandler=defaultHandler;
        this.type=type;
        this.runtimeService=runtimeService;
    }

    @Override
    public String getIncidentHandlerType() {
        return type;
    }

    @Override
    public void handleIncident(IncidentContext context, String message) {
        logger.info("Handling incident, {}", message);
        String notifyUrl = (String) runtimeService.getVariable(context.getExecutionId(), "notifyUrl");
        Message msg = new Message(Message.Type.INCIDENT_CREATED, message);
        MessageHelper.sendMessage(notifyUrl, msg);
        defaultHandler.handleIncident(context,message);

    }

    @Override
    public void resolveIncident(IncidentContext context) {
        logger.info("Resolving incident");
        String notifyUrl = (String) runtimeService.getVariable(context.getExecutionId(), "notifyUrl");
        Message msg = new Message(Message.Type.INCIDENT_RESOLVED, context.getProcessDefinitionId());
        MessageHelper.sendMessage(notifyUrl, msg);
        defaultHandler.resolveIncident(context);
    }

    @Override
    public void deleteIncident(IncidentContext context) {
        logger.info("Deleting incident");
        defaultHandler.deleteIncident(context);
    }

}
