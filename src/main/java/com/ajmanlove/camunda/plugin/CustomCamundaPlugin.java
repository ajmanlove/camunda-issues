package com.ajmanlove.camunda.plugin;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CustomCamundaPlugin extends AbstractProcessEnginePlugin {
    private static final Logger logger = LoggerFactory.getLogger(CustomCamundaPlugin.class);


    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        logger.info("preInit");

        // BpmnParseListeners
        List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
        if(preParseListeners == null) {
            preParseListeners = new ArrayList<BpmnParseListener>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }
        preParseListeners.add(new UserTaskListener());

        // IncidentHandlers
        List<IncidentHandler> incidentHandlers = processEngineConfiguration.getCustomIncidentHandlers();
        if (incidentHandlers==null) {
            incidentHandlers=new ArrayList<>();
            processEngineConfiguration.setCustomIncidentHandlers(incidentHandlers);
        }


        IncidentHandler extHandler = new CustomIncidentHandler(
                new DefaultIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE),
                Incident.EXTERNAL_TASK_HANDLER_TYPE,
                processEngineConfiguration.getRuntimeService());

        IncidentHandler jobHandler = new CustomIncidentHandler(
                new DefaultIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE),
                Incident.FAILED_JOB_HANDLER_TYPE,
                processEngineConfiguration.getRuntimeService());

        incidentHandlers.add(extHandler);
        incidentHandlers.add(jobHandler);
    }
}
