package org.qumodo.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.qumodo.network.QClientSocketListener;
import org.qumodo.network.QTCPSocket;

@SuppressWarnings("FieldCanBeLocal")
public class QTCPSocketService extends Service implements Runnable, QClientSocketListener {

    private static final String LOG_TAG = "QTCPSocketService";
    public static final String INTENT_KEY_HOSTNAME = "org.qumodo.MiscaClient.QTCPSocketService.key.HOSTNAME";
    public static final String INTENT_KEY_PORT = "org.qumodo.MiscaClient.QTCPSocketService.key.PORT";
    public static final String INTENT_KEY_MESSAGE = "org.qumodo.MiscaClient.QTCPSocketService.key.MESSAGE";
    public static final String INTENT_KEY_ERROR = "org.qumodo.MiscaClient.QTCPSocketService.key.ERROR";
    public static final String ACTION_SEND_MESSAGE = "org.qumodo.MiscaClient.QTCPSocketService.action.SEND_MESSAGE";
    public static final String ACTION_CLOSE_SOCKET = "org.qumodo.MiscaClient.QTCPSocketService.action.CLOSE_SOCKET";
    public static final String ACTION_RESTART_SOCKET = "org.qumodo.MiscaClient.QTCPSocketService.action.RESTART_SOCKET";
    public static final String DELEGATE_RECEIVED_MESSAGE = "org.qumodo.MiscaClient.QSocketServer.delegate.RECEIVED_MESSAGE";
    public static final String DELEGATE_SEND_ERROR = "org.qumodo.MiscaClient.QSocketServer.delegate.SEND_ERROR";
    public static final String DELEGATE_SOCKET_ERROR = "org.qumodo.MiscaClient.QSocketServer.delegate.SOCKET_ERROR";
    public static final String DELEGATE_SOCKET_CLOSED = "org.qumodo.MiscaClient.QSocketServer.delegate.SOCKET_CLOSED";
    public static final String DELEGATE_SOCKET_CONNECTION = "org.qumodo.MiscaClient.QSocketServer.delegate.SOCKET_CONNECTION";

    private Thread thread = null;

    QTCPSocket socketClient;

    private void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    private boolean running = true;

    void stopThread() {
        this.running = false;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            switch (action) {
                case ACTION_SEND_MESSAGE:
                    String message = intent.getStringExtra(INTENT_KEY_MESSAGE);
                    if (message != null && !message.isEmpty()) {
                        sendMessageToSocket(message);
                    }
                    break;
                case ACTION_CLOSE_SOCKET:
                    tearDownSocket();
                    break;
                case ACTION_RESTART_SOCKET:
                    startSocket();
                    break;
            }
        }
    };

    public void sendMessageToSocket(String message) {
        Log.d(LOG_TAG, "Send Message to socket");
        if (socketClient.isConnected()) {
            socketClient.sendMessage(message);
        } else {
            broadcastError(DELEGATE_SEND_ERROR, "Socket not connected!", message);
            stopThread();
        }
    }

    private void broadcastMessage(String delegationEvent, String message) {
        Log.d(LOG_TAG, "Broadcast Message: "+delegationEvent+", "+message);
        Intent messageIntent = new Intent();
        messageIntent.setAction(delegationEvent);
        messageIntent.putExtra(INTENT_KEY_MESSAGE, message);
        sendBroadcast(messageIntent);
    }

    private void broadcastError(String delegationError, String errorMessage, String message) {
        Log.d(LOG_TAG, "Broadcast Error");
        Intent errorIntent = new Intent();
        errorIntent.setAction(delegationError);
        errorIntent.putExtra(INTENT_KEY_ERROR, errorMessage);
        if (message != null) {
            errorIntent.putExtra(INTENT_KEY_MESSAGE, message);
        }
        sendBroadcast(errorIntent);
    }

    String hostName;
    int portNumber;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start Command Service");

        hostName = intent.getStringExtra(INTENT_KEY_HOSTNAME);
        portNumber = intent.getIntExtra(INTENT_KEY_PORT, 9500);

        startSocket();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SEND_MESSAGE);
        filter.addAction(ACTION_CLOSE_SOCKET);
        filter.addAction(ACTION_RESTART_SOCKET);
        registerReceiver(receiver, filter);

        return Service.START_REDELIVER_INTENT;
    }

    private void startSocket() {
        if (socketClient == null) {
            Log.d(LOG_TAG, "Making socket");
            socketClient = new QTCPSocket(hostName, portNumber, this);
            startThread();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(LOG_TAG, "CONFIG CHANGE");
        // ignore
    }

    @Override
    public void onLowMemory() {
        Log.d(LOG_TAG, "LOW MEMORY ERROR");
        // ignore
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "SocketService onDestroy");
        super.onDestroy();
        unregisterReceiver(receiver);
        tearDownSocket();
    }

    private void tearDownSocket() {
        Log.d(LOG_TAG, "Tear Down Socket Command...");
        if (socketClient.isConnected() || !socketClient.isClosed()) {
            socketClient.closeSocket();
        }
        stopThread();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "Service Run Invoked");
        socketClient.makeConnection();
        Log.d(LOG_TAG, "Running");
        while (running) {
            Log.d(LOG_TAG, "** RUN **");
            socketClient.readInputStream();
        }
    }

    @Override
    public void socketConnectionEstablished() {
        Log.d(LOG_TAG, "socketConnectionEstablished");
        broadcastMessage(DELEGATE_SOCKET_CONNECTION, "Socket connected");
    }

    @Override
    public void SocketConnectionError(Exception error) {
        Log.d(LOG_TAG, "SocketConnectionError");
        broadcastError(DELEGATE_SOCKET_ERROR, "Socket connection error", null);
        this.tearDownSocket();
    }

    @Override
    public void socketClosed() {
        Log.d(LOG_TAG, "socketClosed");
        broadcastMessage(DELEGATE_SOCKET_CLOSED, "Socket has closed");
        this.tearDownSocket();
    }

    @Override
    public void socketData(String data) {
        Log.d(LOG_TAG, "socketData");
        broadcastMessage(DELEGATE_RECEIVED_MESSAGE, data);
    }
}
