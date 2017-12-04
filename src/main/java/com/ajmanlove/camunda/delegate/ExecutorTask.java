package com.ajmanlove.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutorTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        List<String> execute = (List<String>)execution.getVariable("execute");
        Map<String, Boolean> planner = (Map<String,Boolean>)execution.getVariable("planner");
        if (planner==null) {
            planner=new HashMap<>();
            for (String exec : execute) {
                planner.put(exec, false);
            }
        }

        List<String> exec = new ArrayList<>();
        for (String s: execute) {
            if (!planner.get(s)) {
                exec.add(s);
                break;
            }
        }

        execution.setVariable("planner", planner);
        execution.setVariable("exec", exec);
    }
}
