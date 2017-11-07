package org.qumodo.data.models;

public class MiscaWorkflowImage extends MiscaWorkflowStep {

    public enum MiscaWorkflowImageSource {
        CORE_IMAGE,
        USER_IMAGE
    }

    private MiscaWorkflowImageSource source;
    private String reference, message;

    public MiscaWorkflowImage(String id, MiscaWorkflowImageSource source, String reference, String message) {
        this.id = id;
        this.type = MiscaWorkflowType.IMAGE;
        this.source = source;
        this.reference = reference;
        this.message = message;
    }

    public MiscaWorkflowImageSource getSource() {
        return source;
    }

    public String getReference() {
        return reference;
    }

    public String getMessage() {
        return message;
    }
}
