package org.qumodo.miscaclient;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.MessageCenter;
import org.qumodo.miscaclient.dataProviders.GroupsContentProvider;
import org.qumodo.miscaclient.dataProviders.ServerDetails;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.services.QTCPSocketService;

public class QMiscaClientApplication extends Application {

    private static final String TAG = "QMiscaClientApplication";

    private static int port;
    private static String hostname;
    public static final String APPLICATION_TEAR_SOCKET_DOWN = "org.qumodo.misca.QMiscaClientApplication.TEAR_DOWN_SOCKET";
    public static final String APPLICATION_CONNECT_SOCKET = "org.qumodo.misca.QMiscaClientApplication.CONNECT_SOCKET";
    public Boolean socketServiceActive = true;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (
            action.equals(QTCPSocketService.DELEGATE_SOCKET_ERROR)
            || action.equals(QTCPSocketService.DELEGATE_SOCKET_CLOSED)
        ) {
            socketServiceActive = false;
            UserSettingsManager.setUserAuthorised(false);
            Log.d(TAG, "Socket Closed");
            Toast.makeText(getApplicationContext(), "Connection to MISCA has closed", Toast.LENGTH_SHORT)
                 .show();
        } else if (action.equals(QTCPSocketService.DELEGATE_SOCKET_CONNECTION)) {
            socketServiceActive = true;
            Log.d(TAG, "Socket connected");
            sendUserCredentials();
        } else if (action.equals(APPLICATION_TEAR_SOCKET_DOWN) && socketServiceActive) {
            Intent closeSocketIntent = new Intent();
            closeSocketIntent.setAction(QTCPSocketService.ACTION_CLOSE_SOCKET);
            sendBroadcast(closeSocketIntent);
            socketServiceActive = false;
        } else if (action.equals(APPLICATION_CONNECT_SOCKET) && !socketServiceActive) {
            startTCPSocket();
        }
        }
    };

    private void sendUserCredentials() {
        try {
            String userID = UserSettingsManager.getUserID();
            String deviceID = UserSettingsManager.getDeviceID();
            String hashedDeviceCertificate = UserSettingsManager.getHashedClientPublicKeyString();

            if (userID != null && deviceID != null && hashedDeviceCertificate != null) {

                JSONObject data = new JSONObject();
                data.put(QMessage.KEY_DEVICE_ID, deviceID);
                data.put(QMessage.KEY_PUBLIC_KEY_HASH, hashedDeviceCertificate);
                data.put(QMessage.KEY_PASS_PHRASE, "No_passphrase_yet");

                QMessage credentials = new QMessage("", userID, QMessageType.NEW_CONNECTION, data);

                Intent messagePackage = new Intent();
                messagePackage.setAction(QTCPSocketService.ACTION_SEND_MESSAGE);
                messagePackage.putExtra(QTCPSocketService.INTENT_KEY_MESSAGE, credentials.serialize());
                sendBroadcast(messagePackage);
            } else {
                Log.e(TAG, "Error loading defaults from UserSettingsManager");
                Toast.makeText(getApplicationContext(), "Error loading user defaults", Toast.LENGTH_SHORT)
                     .show();
            }
        } catch (Exception error) {
            Log.e(TAG, "Failed to create user credentials");
            Toast.makeText(getApplicationContext(), "Failed to create user credentials", Toast.LENGTH_SHORT)
                 .show();
            error.printStackTrace();
        }
    }

    private MessageCenter messageCenter;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(QTCPSocketService.DELEGATE_SOCKET_CONNECTION);
        filter.addAction(QTCPSocketService.DELEGATE_SOCKET_CLOSED);
        filter.addAction(QTCPSocketService.DELEGATE_SOCKET_ERROR);
        filter.addAction(QTCPSocketService.DELEGATE_SEND_ERROR);
        filter.addAction(APPLICATION_CONNECT_SOCKET);
        filter.addAction(APPLICATION_TEAR_SOCKET_DOWN);
        registerReceiver(receiver, filter);

        hostname = ServerDetails.getSocketHostName();
        port = ServerDetails.getSocketPortNumber();

        UserSettingsManager.loadSharedPreferences(getApplicationContext());
        setupDataProviders();

        messageCenter = new MessageCenter(getApplicationContext());

        if (UserSettingsManager.getUserID() != null) {
            startTCPSocket();
        }
    }

    private void setupDataProviders() {
        MediaLoader.setContext(getApplicationContext());
        GroupsContentProvider.setup(getApplicationContext());
    }

    private void startTCPSocket() {
        Intent socketService = new Intent(this, QTCPSocketService.class);
        socketService.putExtra(QTCPSocketService.INTENT_KEY_HOSTNAME, hostname);
        socketService.putExtra(QTCPSocketService.INTENT_KEY_PORT, port);
        startService(socketService);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
