package org.qumodo.data.models;

public abstract class MiscaWorkflowStep {

    public enum MiscaWorkflowType {
        QUESTION,
        COMMAND,
        MESSAGE,
        IMAGE
    }

    protected MiscaWorkflowType type;
    protected String id;

    public MiscaWorkflowType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

}
