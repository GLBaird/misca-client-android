package org.qumodo.miscaclient.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DataManager;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.MessageCenter;
import org.qumodo.data.models.EnrichmentData;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.MiscaWorkflowCommand.MiscaWorkflowCommands;
import org.qumodo.miscaclient.dataProviders.DataEnrichmentProvider;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.ServerDetails.SocketCommands;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.services.QTCPSocketService;

import java.util.ArrayList;
import java.util.List;



public class MiscaCommandRunner {

    private Context context;
    private MiscaWorkflowCommands command;
    private DataManager dm;
    private String imageID;
    private String groupID;

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

    public static void addMiscaMessage(String message, String groupID, Context context) {
        DataManager dm = new DataManager(context);
        MiscaCommandRunner commandRunner = new MiscaCommandRunner(context, null, groupID, MiscaWorkflowCommands.DO_NOTHING, dm);
        commandRunner.addMiscaTextMessage(message);
    }

    public static void runObjectDetection(String classification, String groupID, Context context) {
        DataManager dm = new DataManager(context);
        MiscaCommandRunner commandRunner = new MiscaCommandRunner(context, null, groupID, MiscaWorkflowCommands.DO_NOTHING, dm);
        commandRunner.getObjectInformation(classification);
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
        CropImage.activity(MediaLoader.getImageURI(imageID, MediaLoader.IMAGE_STORE_UPLOADS, context))
                .setAllowRotation(true)
                .setActivityTitle("Crop to object")
                .start((Activity) context);
    }

    public void addMiscaTextMessage(String text) {
        Message m = dm.addNewMessage(text, QMessageType.MISCA_TEXT, groupID, null,
                UserSettingsManager.getMiscaID(), null);
        MessageContentProvider.addItem(m);
        updateListUI();
    }

    private void runObjectDetection() {
        EnrichmentData data = DataEnrichmentProvider.getProvider().getDataWithID(imageID);
        if (data != null) {
            processObjectDetectionData(data);
        } else {
            addMiscaTextMessage("Object recognition is being performed on the image above.");
            final CountDownTimer timer = new CountDownTimer(6000, 6000) {
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    addMiscaTextMessage("Waiting for object data.");
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
                    "Please try again with a different picture");
        }
    }

    public void processObjectDetection(String classification) {
        if (classification == null || classification.isEmpty()) {
            addMiscaTextMessage("No object was detected in the photo. " +
                    "Please try again with a different picture");
        } else {
            getObjectInformation(classification);
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
        EnrichmentData data = DataEnrichmentProvider.getProvider().getDataWithID(imageID);
        if (data != null) {
            processANPRData(data);
        } else {
            addMiscaTextMessage("ANPR is being performed on the image above.");

            final CountDownTimer timer = new CountDownTimer(6000, 6000) {
                @Override
                public void onTick(long l) { }

                @Override
                public void onFinish() {
                    addMiscaTextMessage("Waiting for ANPR data.");
                }
            };
            timer.start();
            DataEnrichmentProvider.getProvider().addListener(imageID,
                    new DataEnrichmentProvider.DataEnrichmentListener() {
                        @Override
                        public void enrichmentDataReady(EnrichmentData data) {
                            timer.cancel();
                            processANPRData(data);
                        }
                    });
        }
    }

    private void processANPRData(EnrichmentData enriched) {
        String[] plates = enriched.getANPR();
        if (plates != null && plates.length >= 1) {
            String search = TextUtils.join(" ", plates);
            List<String> found = new ArrayList<>(plates.length);
            for (String plate : plates) {
                found.add(plate.split(" ")[0]);
            }
            addMiscaTextMessage("Number plates found: " + TextUtils.join(", ", found) + ",\nsearching database...");
            try {
                JSONObject data = new JSONObject();
                data.put("command", SocketCommands.MISCA_ANPR_SEARCH);
                data.put("anpr", search);
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
        } else {
            addMiscaTextMessage("No ANPR was detected in the photo. " +
                    "Please try again with a different picture");
        }
    }

    private void runFaceDetection() {
        EnrichmentData data = DataEnrichmentProvider.getProvider().getDataWithID(imageID);
        if (data != null) {
            processFaceDetection(data);
        } else {
            addMiscaTextMessage("Face detection is being performed on the image above.");

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
                            processFaceDetection(data);
                        }
                    });
        }
    }

    private void processFaceDetection(EnrichmentData data) {
        Rect[] faces = data.getFaces();
        if (faces != null && faces.length > 0) {
            Message faceMessage = dm.addNewMessage(imageID, QMessageType.MISCA_FACES, groupID,
                    null, UserSettingsManager.getMiscaID(), null);
            addMiscaTextMessage("Found the following faces:");
            MessageContentProvider.addItem(faceMessage);
            updateListUI();
        } else {
            addMiscaTextMessage("No faces were detected in the photo. " +
                    "Please try again with a different picture.");
        }
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
