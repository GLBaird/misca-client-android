package org.qumodo.miscaclient.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DataManager;
import org.qumodo.data.MessageCenter;
import org.qumodo.data.models.EnrichmentData;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.MiscaWorkflowCommand.MiscaWorkflowCommands;
import org.qumodo.miscaclient.dataProviders.DataEnrichmentProvider;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.miscaclient.dataProviders.ServerDetails.SocketCommands;
import org.qumodo.services.QTCPSocketService;


public class MiscaCommandRunner {

    Context context;
    MiscaWorkflowCommands command;
    DataManager dm;
    String imageID;
    String groupID;

    private MiscaCommandRunner(Context context, String imageID, String groupID,
                               MiscaWorkflowCommands command, DataManager dm) {
        this.context = context;
        this.command = command;
        this.imageID = imageID;
        this.groupID = groupID;
        this.dm = dm;
    }

    public static void runCommand(MiscaWorkflowCommands command, String imageID,
                                  String groupID, Context context, DataManager dm) {
        MiscaCommandRunner commandRunner = new MiscaCommandRunner(context, imageID,
                groupID, command, dm);
        commandRunner.run();
    }

    private void run() {
        switch (command) {
            case OBJECT_DETECTION:
                runObjectDetection();
                break;
            case OBJECT_DETECTION_CROP:
                runObjectDetectionCrop();
                break;
            case NUMBER_PLATE_DETECTION:
                runNumberPlateDetection();
                break;
            case FACE_DETECTION:
                runFaceDetection();
                break;
            case MISCA_RESPONSE_QUESTION:
                runMiscaQuestionResponse();
                break;
        }
    }

    private void runObjectDetectionCrop() {
        Toast.makeText(context, "Object Detection Crop", Toast.LENGTH_SHORT).show();
    }

    private void addMiscaTextMessage(String text) {
        Message m = dm.addNewMessage(text, QMessageType.MISCA_TEXT, groupID, null,
                UserSettingsManager.getMiscaID(), null);
        MessageContentProvider.addItem(m);
        updateListUI();
    }

    private void runObjectDetection() {
        EnrichmentData data = DataEnrichmentProvider.getProvider().getDataWithID(imageID);
        if (data != null) {
            Log.d("COMMAND", "DATA FOUND");
            processObjectDetectionData(data);
        } else {
            Log.d("COMMAND", "NOT FOUND");
            addMiscaTextMessage("Object recognition is being performed on the image above.");

            final CountDownTimer timer = new CountDownTimer(6000, 6000) {
                @Override
                public void onTick(long l) { }

                @Override
                public void onFinish() {
                    addMiscaTextMessage("No response from server. " +
                            "Probably something has gone wrong.");
                }
            };
            timer.start();
            DataEnrichmentProvider.getProvider().addListener(imageID,
                    new DataEnrichmentProvider.DataEnrichmentListener() {
                @Override
                public void enrichmentDataReady(EnrichmentData data) {
                    timer.cancel();
                    processObjectDetectionData(data);
                }
            });
        }
    }

    private void processObjectDetectionData(EnrichmentData data) {
        if (data.getClassification() != null) {
            getObjectInformation(data.getClassification());
        } else {
            addMiscaTextMessage("No object was detected in the photo. " +
                    "Please try again with a different photo");
        }
    }

    private void getObjectInformation(String classification) {
        addMiscaTextMessage("Object classified as: " + classification
                + "\nMatching object against the database...");
        try {
            JSONObject data = new JSONObject();
            data.put("command", SocketCommands.MISCA_OBJECT_SEARCH);
            data.put("classification", classification);
            data.put("groupID", groupID);
            QMessage message = new QMessage(
                    UserSettingsManager.getMiscaID(),
                    UserSettingsManager.getUserID(),
                    QMessageType.COMMAND,
                    data
            );

            Intent sendMessage = new Intent();
            sendMessage.setAction(QTCPSocketService.ACTION_SEND_MESSAGE);
            sendMessage.putExtra(QTCPSocketService.INTENT_KEY_MESSAGE, message.serialize());
            context.sendBroadcast(sendMessage);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void runNumberPlateDetection() {
        Toast.makeText(context, "Number Plate", Toast.LENGTH_SHORT).show();
    }

    private void runFaceDetection() {
        Toast.makeText(context, "Face", Toast.LENGTH_SHORT).show();
    }

    private void updateListUI() {
        Intent updateUI = new Intent();
        updateUI.setAction(MessageCenter.NEW_LIST_ITEM);
        context.sendBroadcast(updateUI);
    }

    private void runMiscaQuestionResponse() {
        Log.d("Command", "com: " + command +", imID: " + imageID);

        int objectIndex = Integer.parseInt(imageID.split("::")[0]);
        String messageID = imageID.split("::")[1];
        MiscaWorkflowManager.getManager().removeWorkflowStep(messageID);

        MiscaResponseController.getController().sendFinalResponse(messageID, context, objectIndex);
    }

}
