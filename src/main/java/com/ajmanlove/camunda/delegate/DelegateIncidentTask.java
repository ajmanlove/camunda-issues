package com.ajmanlove.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class DelegateIncidentTask implements JavaDelegate {


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        throw new RuntimeException("Creating Foo Incident");
    }
}
