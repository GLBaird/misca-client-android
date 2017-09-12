package org.qumodo.miscaclient;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.qumodo.miscaclient.dataProviders.GroupsContentProvider;
import org.qumodo.services.QTCPSocketService;

public class    QMiscaClientApplication extends Application {

    private static final int port = 9500;
    private static final String hostname = "192.168.0.19";
    public static final String APPLICATION_TEAR_SOCKET_DOWN = "org.qumodo.misca.QMiscaClientApplication.TEAR_DOWN_SOCKET";
    public static final String APPLICATION_CONNECT_SOCKET = "org.qumodo.misca.QMiscaClientApplication.CONNECT_SOCKET";
    public Boolean socketServiceActive = true;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(QTCPSocketService.DELEGATE_SOCKET_ERROR)
                    || action.equals(QTCPSocketService.DELEGATE_SOCKET_CLOSED)
                    || action.equals(QTCPSocketService.DELEGATE_SEND_ERROR)) {
                socketServiceActive = false;
                Log.d("MAIN APP", "Socket Closed");
            } else if (action.equals(QTCPSocketService.DELEGATE_SOCKET_CONNECTION)) {
                socketServiceActive = true;
                Log.d("MAIN APP", "Socket connected");
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

        setupDataProviders();

        startTCPSocket();
    }

    private void setupDataProviders() {
        GroupsContentProvider.setup(getApplicationContext());
    }

    private void startTCPSocket() {
        Log.d("MAIN APP", "Launch Service");
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
