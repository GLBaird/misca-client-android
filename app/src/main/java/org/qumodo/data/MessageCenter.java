package org.qumodo.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.network.QMessage;
import org.qumodo.services.QTCPSocketService;
import org.qumodo.network.QMessageType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

public class MessageCenter {

    public static final String TAG = "MessageCenter";

    public static final String USER_AUTHORISED = "org.qumodo.data.MessageCenter.UserAuthorised";
    public static final String RELOAD_UI = "org.qumodo.data.MessageCenter.ReloadUI";
    public static final String NEW_LIST_ITEM = "org.qumodo.date.MessageCenter.NewListItem";
    public static final String INTENT_KEY_GROUP_ID = "org.qumodo.data.MessageCenter.GroupID";

    private static Context appContext;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String message;

            switch (action) {
                case QTCPSocketService.DELEGATE_RECEIVED_MESSAGE:
                    Log.d(TAG, "Message Received");
                    message = intent.getStringExtra(QTCPSocketService.INTENT_KEY_MESSAGE);
                    if (message != null && !message.isEmpty())
                        checkMessageContent(message);
                    break;
                case QTCPSocketService.DELEGATE_SEND_ERROR:
                    message = intent.getStringExtra(QTCPSocketService.INTENT_KEY_MESSAGE);
                    if (message != null && !message.isEmpty())
                        checkMessageContent(message);
                    break;
            }
        }
    };

    public MessageCenter(Context context) {
        appContext = context.getApplicationContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction(QTCPSocketService.DELEGATE_RECEIVED_MESSAGE);
        filter.addAction(QTCPSocketService.DELEGATE_SEND_ERROR);

        appContext.registerReceiver(broadcastReceiver, filter);
    }

    private void checkMessageContent(String message) {
        Log.d(TAG, "Message data received: " + message);
        QMessage parsed = parseMessage(message);
        if (parsed != null) {
            if (parsed.type == QMessageType.AUTHENTICATION) {
                getUserAuthenticationDetails(parsed);
            } else if (parsed.type == QMessageType.TEXT) {
                parseTextMessage(parsed);
            } else if (parsed.type == QMessageType.PICTURE) {
                parsePictureMessage(parsed);
            } else if (parsed.type == QMessageType.MISCA_QUESTION || parsed.type == QMessageType.MISCA_RESPONSE) {
                parseMiscaMessage(parsed);
            } else if (parsed.type == QMessageType.COMMAND) {
                parseSystemCommand(parsed);
            } else if (parsed.type == QMessageType.ERROR) {
                Log.d(TAG, "Error from socket: " + parsed);
            } else if (parsed.type == QMessageType.STATUS) {
                Log.d(TAG, "Status Message from Socket: " + parsed);
            }
        }
    }

    private void getUserAuthenticationDetails(QMessage message) {
        try {
            boolean authenticated = message.getBool(QMessage.KEY_USER_AUTHENTICATION);
            String miscaID = message.data.getString(QMessage.KEY_MISCA_ID);
            if (authenticated) {
                UserSettingsManager.setUserAuthorised(true);
                Toast.makeText(appContext, "User Authenticated", Toast.LENGTH_SHORT)
                     .show();
                UserSettingsManager.setMiscaID(miscaID);
                Intent userAuthorised = new Intent();
                userAuthorised.setAction(USER_AUTHORISED);
                appContext.sendBroadcast(userAuthorised);
            } else {
                UserSettingsManager.setUserAuthorised(false);
                Log.e(TAG, "Failed user authentication");
                Toast.makeText(appContext, "Failed user authentication", Toast.LENGTH_SHORT)
                     .show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse message repose for authentication");
            e.printStackTrace();
            Toast.makeText(appContext, "Failed to parse message response!", Toast.LENGTH_SHORT)
                 .show();
        }
    }

    private void parseTextMessage(QMessage message) {
        try {
            String messageText = null;
            try {
                messageText = URLDecoder.decode(message.data.getString(QMessage.KEY_MESSAGE), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                messageText = message.data.getString(QMessage.KEY_MESSAGE);
            }
            String groupID = message.data.getString(QMessage.KEY_GROUP_ID);
            DataManager dm = new DataManager(appContext);
            Message newMessage = dm.addNewMessage(messageText, message.type, groupID, message.id, message.from, new Date(message.ts));

            String currentGroupID = MessageContentProvider.getGroupID();
            if (currentGroupID != null && currentGroupID.equals(groupID)) {
                MessageContentProvider.addItem(newMessage);
                Intent updateUI = new Intent();
                updateUI.setAction(NEW_LIST_ITEM);
                updateUI.putExtra(QMessage.KEY_GROUP_ID, groupID);
                appContext.sendBroadcast(updateUI);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePictureMessage(QMessage message) {
        try {
            String caption = message.data.getString(QMessage.KEY_CAPTION);
            String groupID = message.data.getString(QMessage.KEY_GROUP_ID);

            DataManager dm = new DataManager(appContext);
            Message newMessage = dm.addNewMessage(caption, message.type, groupID, message.id, message.from, new Date(message.ts));
            MessageContentProvider.addItem(newMessage);

            Intent updateUI = new Intent();
            updateUI.setAction(RELOAD_UI);
            updateUI.putExtra(QMessage.KEY_GROUP_ID, groupID);
            appContext.sendBroadcast(updateUI);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseMiscaMessage(QMessage message) {

    }

    private void parseSystemCommand(QMessage message) {

    }

    private void reportError(String message) {
        QMessage parsed = parseMessage(message);
        if (parsed != null) {
            DataManager dm = new DataManager(appContext);
            dm.setMessageError(parsed.id, true);
            MessageContentProvider.updateMessageError(parsed.id, true);
            try {
                updateUI(parsed.data.getString(QMessage.KEY_GROUP_ID));
            } catch (JSONException e) {
                e.printStackTrace();
                updateUI(null);
            }
        }
    }

    private QMessage parseMessage(String message) {
        try {
            return QMessage.make(message);
        } catch (JSONException e) {
            Log.d("MessageCenter", "Failed to parse message: " + message);
            e.printStackTrace();
        }

        return null;
    }

    private void updateUI(String groupID) {
        Intent updateUIMessage = new Intent();
        updateUIMessage.setAction(RELOAD_UI);
        if (groupID != null)
            updateUIMessage.putExtra(INTENT_KEY_GROUP_ID, groupID);
        appContext.sendBroadcast(updateUIMessage);
    }

}
