package org.qumodo.miscaclient.controllers;

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
                getStartID(), "Would you like me to run a search on\u00A0the\u00A0image?", new Question[]{
                new Question("No", "END"),
                new Question("Yes", "start_search")
        }));

        workflow.put("anpr_extra_question", new MiscaWorkflowQuestion(
                "anpr_extra_question", "Would you like me to search for extra data on the current registered owner?", new Question[]{
                new Question("No", "END"),
                new Question("Yes", "anpr_extra")
        }));


        workflow.put("start_search", new MiscaWorkflowQuestion(
                "start_search", "Would you like to search\u00A0for:", new Question[]{
                    new Question("Face", "face_detect_start"),
                    new Question("Object", "crop_image_question"),
                    new Question("Number plate", "anpr_start")
        }));

        workflow.put("crop_image_question", new MiscaWorkflowQuestion(
                "crop_image_question", "Would you like to crop the image to\u00A0the\u00A0object?", new Question[]{
                new Question("No", "object_detection"),
                new Question("Yes", "object_detection_crop")
        }));

        workflow.put("object_detection", new MiscaWorkflowCommand(
                "detect_object", MiscaWorkflowCommands.OBJECT_DETECTION));

        workflow.put("object_detection_crop", new MiscaWorkflowCommand(
                "object_detection_crop", MiscaWorkflowCommands.OBJECT_DETECTION_CROP));

        workflow.put("face_detect_start", new MiscaWorkflowCommand(
                "face_detect_start", MiscaWorkflowCommands.FACE_DETECTION));

        workflow.put("anpr_start", new MiscaWorkflowCommand(
                "anpr_start", MiscaWorkflowCommands.NUMBER_PLATE_DETECTION));

        workflow.put("anpr_extra", new MiscaWorkflowCommand(
                "anpr_extra", MiscaWorkflowCommands.NUMBER_PLATE_DETECTION_EXTRA));

        workflow.put("response_question", new MiscaWorkflowCommand(
                "response_question", MiscaWorkflowCommands.MISCA_RESPONSE_QUESTION));

        return workflow;
    }

}
