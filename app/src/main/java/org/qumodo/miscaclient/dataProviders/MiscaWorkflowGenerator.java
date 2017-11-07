package org.qumodo.miscaclient.dataProviders;

import android.util.Log;

import org.qumodo.data.models.MiscaWorkflowCommand;
import org.qumodo.data.models.MiscaWorkflowCommand.MiscaWorkflowCommands;
import org.qumodo.data.models.MiscaWorkflowQuestion;
import org.qumodo.data.models.MiscaWorkflowQuestion.Question;
import org.qumodo.data.models.MiscaWorkflowStep;

import java.util.HashMap;
import java.util.Map;

public class MiscaWorkflowGenerator {

    public static String getStartID() {
        return "000";
    }

    public static Map<String, MiscaWorkflowStep> generateWorkflow() {
        HashMap<String, MiscaWorkflowStep> workflow = new HashMap<>();

        workflow.put(getStartID(), new MiscaWorkflowQuestion(
                getStartID(), "Would you like me to run a search on the image?", new Question[]{
                new Question("Yes", "010"),
                new Question("No", "END")
        }));

        workflow.put("010", new MiscaWorkflowQuestion(
                "010", "Would you like to search for a:", new Question[]{
                    new Question("Face", "020"),
                    new Question("Object", "030"),
                    new Question("Number plate?", "040")
        }));

        workflow.put("020", new MiscaWorkflowCommand(MiscaWorkflowCommands.FACE_DETECTION));
        workflow.put("030", new MiscaWorkflowCommand(MiscaWorkflowCommands.OBJECT_DETECTION));
        workflow.put("040", new MiscaWorkflowCommand(MiscaWorkflowCommands.NUMBER_PLATE_DETECTION));

        return workflow;
    }

}
