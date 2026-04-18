package com.fairtix.fraud.application;

public class StepUpRequiredException extends RuntimeException {

    private final String action;

    public StepUpRequiredException(String action) {
        super("Step-up verification required for action: " + action);
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
