package org.qumodo.data.models;

public class MiscaWorkflowMessage extends MiscaWorkflowStep {

    private String message;

    public MiscaWorkflowMessage(String id, String message) {
        this.id = id;
        this.type = MiscaWorkflowType.MESSAGE;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
