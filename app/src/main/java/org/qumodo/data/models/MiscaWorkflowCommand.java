package org.qumodo.data.models;

public class MiscaWorkflowCommand extends MiscaWorkflowStep {

    public static enum MiscaWorkflowCommands {
        FACE_DETECTION,
        OBJECT_DETECTION,
        OBJECT_DETECTION_CROP,
        NUMBER_PLATE_DETECTION,
        NUMBER_PLATE_DETECTION_EXTRA,
        MISCA_RESPONSE_QUESTION,
        DO_NOTHING
    }

    private MiscaWorkflowCommands command;

    public MiscaWorkflowCommand(String id, MiscaWorkflowCommands command) {
        this.id = id;
        this.type = MiscaWorkflowType.COMMAND;
        this.command = command;
    }

    public MiscaWorkflowCommands getCommand() {
        return command;
    }

}
