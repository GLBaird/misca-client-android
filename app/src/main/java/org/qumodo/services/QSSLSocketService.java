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

import org.qumodo.network.QSSLClientSocket;
import org.qumodo.network.QClientSocketListener;
import org.qumodo.network.QTCPSocket;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class QSSLSocketService extends Service implements Runnable, QClientSocketListener {

    private static final String LOG_TAG = "QTCPSocketService";
    public static final String INTENT_KEY_HOSTNAME = "org.qumodo.MiscaClient.QTCPSocketService.key.HOSTNAME";
    public static final String INTENT_KEY_PORT = "org.qumodo.MiscaClient.QTCPSocketService.key.PORT";
    public static final String INTENT_KEY_MESSAGE = "org.qumodo.MiscaClient.QTCPSocketService.key.MESSAGE";
    public static final String INTENT_KEY_ERROR = "org.qumodo.MiscaClient.QTCPSocketService.key.ERROR";
    public static final String ACTION_SEND_MESSAGE = "org.qumodo.MiscaClient.QTCPSocketService.action.SEND_MESSAGE";
    public static final String ACTION_CLOSE_SOCKET = "org.qumodo.MiscaClient.QTCPSocketService.action.CLOSE_SOCKET";
    public static final String DELEGATE_RECEIVED_MESSAGE = "org.qumodo.MiscaClient.QSocketServer.delegate.RECEIVED_MESSAGE";
    public static final String DELEGATE_SEND_ERROR = "org.qumodo.MiscaClient.QSocketServer.delegate.SEND_ERROR";
    public static final String DELEGATE_SOCKET_ERROR = "org.qumodo.MiscaClient.QSocketServer.delegate.SOCKET_ERROR";
    public static final String DELEGATE_SOCKET_CLOSED = "org.qumodo.MiscaClient.QSocketServer.delegate.SOCKET_CLOSED";
    public static final String DELEGATE_SOCKET_CONNECTION = "org.qumodo.MiscaClient.QSocketServer.delegate.SOCKET_CONNECTION";

    private Thread thread = null;

    QSSLClientSocket socketClient;

    private void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ACTION_SEND_MESSAGE)) {
                String message = intent.getStringExtra(INTENT_KEY_MESSAGE);
                if (message != null && !message.isEmpty()) {
                    sendMessageToSocket(message);
                }
            } else if (action.equals(ACTION_CLOSE_SOCKET)){
                tearDownSocket();
            }
        }
    };

    public void sendMessageToSocket(String message) {
        Log.d(LOG_TAG, "Send Message to socket");
        if (socketClient.isConnected()) {
            try {
                socketClient.sendData(message);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error sending message");
                e.printStackTrace();
                broadcastError(DELEGATE_SEND_ERROR, e.getMessage());
            }
        } else {
            broadcastError(DELEGATE_SEND_ERROR, "Socket not connected!");
            tearDownSocket();
        }
    }

    private void broadcastMessage(String delegationEvent, String message) {
        Log.d(LOG_TAG, "Broadcast Message");
        Intent messageIntent = new Intent();
        messageIntent.setAction(delegationEvent);
        messageIntent.putExtra(INTENT_KEY_MESSAGE, message);
        sendBroadcast(messageIntent);
    }

    private void broadcastError(String delegationError, String errorMessage) {
        Log.d(LOG_TAG, "Broadcast Error");
        Intent errorIntent = new Intent();
        errorIntent.setAction(delegationError);
        errorIntent.putExtra(INTENT_KEY_ERROR, errorMessage);
        sendBroadcast(errorIntent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start Command Service");
        if (socketClient == null) {
            Log.d(LOG_TAG, "Making socket");
            socketClient = new QSSLClientSocket(getApplicationContext(), this);
        }
        startThread();
        String hostName = intent.getStringExtra(INTENT_KEY_HOSTNAME);
        int port = intent.getIntExtra(INTENT_KEY_PORT, 9500);
        socketClient.setHostNameAndPort(hostName, port);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SEND_MESSAGE);
        filter.addAction(ACTION_CLOSE_SOCKET);
        registerReceiver(receiver, filter);

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore
    }

    @Override
    public void onLowMemory() {
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
            try {
                Log.d(LOG_TAG, "Closing socket onDestroy of service");
                socketClient.closeSocket();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to close socket onDestroy of service");
                e.printStackTrace();
            }
        }
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
        try {
            socketClient.makeConnection();
            Log.d(LOG_TAG, "After Run -- Make Connection finished (Should be infinite until killed)");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to make connection due to IO Error");
            e.printStackTrace();
            broadcastError(DELEGATE_SOCKET_ERROR, "Failed to make connection");
        } catch (KeyManagementException | KeyStoreException | CertificateException e) {
            Log.e(LOG_TAG, "Failed to make socket connection due to certificate problems");
            e.printStackTrace();
            broadcastError(DELEGATE_SOCKET_ERROR, "Keystore or certificate error");
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Failed to make socket connection due to missing Algorithm");
            e.printStackTrace();
            broadcastError(DELEGATE_SOCKET_ERROR, "Missing algorithm error");
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
        broadcastError(DELEGATE_SOCKET_ERROR, "Socket connection error");
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
