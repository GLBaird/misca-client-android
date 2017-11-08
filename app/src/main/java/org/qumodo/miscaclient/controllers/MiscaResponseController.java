package org.qumodo.miscaclient.controllers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DataManager;
import org.qumodo.data.MessageCenter;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.MiscaWorkflowQuestion;
import org.qumodo.data.models.MiscaWorkflowQuestion.Question;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.dataProviders.ServerDetails.SocketCommands;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MiscaResponseController {

    private static MiscaResponseController controller;

    public static MiscaResponseController getController() {
        if (controller == null) {
            controller = new MiscaResponseController();
        }

        return controller;
    }

    private HashMap<String, QMessage> messages = new HashMap<>();

    private MiscaResponseController(){}

    private Question[] getQuestions(String command, JSONArray items) {
        try {
            if (command.equals(SocketCommands.MISCA_OBJECT_SEARCH)) {
                List<Question> questions = new ArrayList<>(items.length());
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = (JSONObject) items.get(i);
                    String object = item.getString("object");
                    Question q = new Question(object, "response_question::"+i);
                    questions.add(q);
                }

                questions.add(new Question("Cancel", "END"));

                return questions.toArray(new Question[questions.size()]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Question[]{};
    }

    public void processResponse(QMessage message, Context context) {
        try {
            messages.put(message.id, message);
            String command  = message.data.getString("command");
            String groupID  = message.data.getString("groupID");
            JSONArray items = message.data.getJSONArray("items");

            Log.d("RESP", "Mes Store " + messages.keySet().toString());

            if (items.length() > 1) {
                Log.d("RESP", "More than one item ID:" + message.id);
                sendMiscaQuestion(groupID, command, message.id,
                        getQuestions(command, items), context);
            } else {
                Log.d("RESP", "Single item");
                sendMessageData(context, command, groupID, (JSONObject) items.get(0));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            sendMessage(MessageContentProvider.getGroupID(),
                    "Error, Failed to extract data from response", context);
        }
    }

    public void sendFinalResponse(String messageID, Context context, int itemIndex) {
        QMessage message = messages.get(messageID);
        if (message != null) {
            try {
                String command  = message.data.getString("command");
                String groupID  = message.data.getString("groupID");
                JSONArray items = message.data.getJSONArray("items");
                JSONObject data = (JSONObject) items.get(itemIndex);

                sendMessageData(context, command, groupID, data);
            } catch (JSONException e) {
                e.printStackTrace();
                sendMessage(MessageContentProvider.getGroupID(),
                        "Error, Failed to extract data from response", context);
            }
        } else {
            sendMessage(MessageContentProvider.getGroupID(), "Error, can't find data from server", context);
        }
    }

    private void sendMessageData(Context context, String command, String groupID, JSONObject item) throws JSONException {
        String image = item.has("image") ? item.getString("image") : null;
        Log.d("RESP", "Misca Image " + image);
        if (image != null) {
            sendMiscaImage(groupID, image, context);
        }
        String responseMessage = getResultText(command, item);
        Log.d("RESP", "Response message " + responseMessage);
        sendMessage(groupID, responseMessage, context);
    }


    private void sendMessage(String groupID, String message, Context context) {
        Log.d("RESP", "Sending message: " + groupID);
        DataManager dm = new DataManager(context);
        Message miscaMessage = dm.addNewMessage(message, QMessageType.MISCA_TEXT, groupID, null, UserSettingsManager.getMiscaID(), null);
        MessageContentProvider.addItem(miscaMessage);

        updateUI(context);
    }

    private void updateUI(Context context) {
        Intent updateUI = new Intent();
        updateUI.setAction(MessageCenter.NEW_LIST_ITEM);
        context.sendBroadcast(updateUI);
    }

    private String getResultText(String command, JSONObject data) {
        if (command.equals(SocketCommands.MISCA_OBJECT_SEARCH)) {
            try {
                String object = data.getString("object");
                String description = data.getString("details");
                return "Object classified as: " + object + "\n" +
                        "Information:\n" + description;
            } catch (JSONException e) {
                e.printStackTrace();
                return "Error, failed to extract object data from response";
            }
        }

        return "Error, unknown command";
    }

    private String getQuestionMessageText(String command) {
        switch (command) {
            case SocketCommands.MISCA_OBJECT_SEARCH:
                return "Here are the objects found in the image, click on the one you wish to lookup:";
        }

        return "Error, unknown command";
    }

    private void sendMiscaImage(String groupID, String path, Context context) {
        Log.d("RESP", "Sending Misca Image " + path);
        DataManager dm = new DataManager(context);
        Message message = dm.addNewMessage(path, QMessageType.MISCA_PHOTO, groupID, null, UserSettingsManager.getMiscaID(), null);
        MessageContentProvider.addItem(message);

        updateUI(context);
    }

    private void sendMiscaQuestion(String groupID, String command, String messageID, Question[] questions, Context context) {
        Log.d("RESP", "Send Question " + Arrays.toString(questions));
        Log.d("RESP", "What is message ID? " + messageID);
        DataManager dm = new DataManager(context);
        MiscaWorkflowManager.getManager().addWorkflowStep(
                messageID,
                new MiscaWorkflowQuestion(messageID, getQuestionMessageText(command), questions)
        );

        Message message = dm.addNewMessage(messageID+"::"+messageID, QMessageType.MISCA_QUESTION, groupID, null, UserSettingsManager.getMiscaID(), null);
        MessageContentProvider.addItem(message);

        updateUI(context);
    }

}
