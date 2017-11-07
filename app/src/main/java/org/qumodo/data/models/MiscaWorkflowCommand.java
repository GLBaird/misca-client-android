package org.qumodo.data.models;

public class MiscaWorkflowCommand extends MiscaWorkflowStep {

    public static enum MiscaWorkflowCommands {
        FACE_DETECTION,
        OBJECT_DETECTION,
        NUMBER_PLATE_DETECTION
    }

    private MiscaWorkflowCommands command;

    public MiscaWorkflowCommand(MiscaWorkflowCommands command) {
        this.type = MiscaWorkflowType.COMMAND;
        this.command = command;
    }

    public MiscaWorkflowCommands getCommand() {
        return command;
    }

}
