package com.ajmanlove.camunda.plugin;

import com.ajmanlove.camunda.plugin.messages.Message;
import com.ajmanlove.camunda.plugin.tasks.UserTaskCreatedListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;

import java.util.concurrent.ArrayBlockingQueue;

public class UserTaskListener extends AbstractBpmnParseListener {
    private final TaskListener listener;


    public UserTaskListener() {
        this.listener = new UserTaskCreatedListener();
    }

    protected void addTaskCreateListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, listener);
    }

    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
        UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
        TaskDefinition taskDefinition = activityBehavior.getTaskDefinition();
        addTaskCreateListeners(taskDefinition);
    }

}
