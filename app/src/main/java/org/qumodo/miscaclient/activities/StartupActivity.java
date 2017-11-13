package org.qumodo.miscaclient.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qumodo.miscaclient.QMiscaClientApplication;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.ServerDetails;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.services.QTCPSocketService;
import org.qumodo.data.MessageCenter;

public class StartupActivity extends Activity implements View.OnClickListener, QMiscaClientApplication.SocketCloseListener {

    public static final String TAG = "StartupActivity";

    Button reconnectButton;
    TextView infoText;
    ProgressBar spinner;
    MenuItem item1, item2;
    boolean firstLaunch = false;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action Received " + action);
            if (action == null) {
                Log.d(TAG, "Action Null");
                return;
            }
            switch (action) {
                case MessageCenter.USER_AUTHORISED:
                    startMainActivity();
                    break;
                case QTCPSocketService.DELEGATE_SOCKET_CLOSED:
                case QTCPSocketService.DELEGATE_SOCKET_ERROR:
                    Log.d(TAG, "Socket error or closed");
                    updateUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Log.d(TAG, "Created");

        if (UserSettingsManager.getUserID() != null && QTCPSocketService.isConnected()) {
            startMainActivity();
            return;
        }

        firstLaunch = savedInstanceState == null;


        reconnectButton = findViewById(R.id.button_try_again);
        infoText = findViewById(R.id.text_connection_info);
        spinner = findViewById(R.id.spinner);

        reconnectButton.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Started");
        QMiscaClientApplication app = (QMiscaClientApplication) getApplicationContext();
        app.setSocketCloseListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageCenter.USER_AUTHORISED);
        filter.addAction(QTCPSocketService.DELEGATE_SOCKET_CONNECTION);
        registerReceiver(receiver, filter);
        updateUI();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        QMiscaClientApplication app = (QMiscaClientApplication) getApplicationContext();
        app.setSocketCloseListener(null);
        unregisterReceiver(receiver);
        super.onStop();
    }


    private void startMainActivity() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
        finish();
    }

    private void updateUI() {
        if (UserSettingsManager.getUserID() == null) {
            infoText.setText("Choose user ID from the upper right menu.");
            reconnectButton.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
        } else if (firstLaunch || QTCPSocketService.isConnected()) {
            infoText.setText("Connecting to server...");
            reconnectButton.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
        } else {
            infoText.setText("Not connected to server.");
            reconnectButton.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        infoText.setText("Connecting to MISCA");
        spinner.setVisibility(View.VISIBLE);
        reconnectButton.setVisibility(View.GONE);

        Intent startSocket = new Intent();
        startSocket.setAction(QMiscaClientApplication.APPLICATION_CONNECT_SOCKET);
        sendBroadcast(startSocket);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_ids_startup, menu);


        String userID = UserSettingsManager.getUserID();

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.action_user_a)
                item1 = menu.getItem(i);
            else if (menu.getItem(i).getItemId() == R.id.action_user_b)
                item2 = menu.getItem(i);
        }

        if (userID != null && userID.equals(UserSettingsManager.USER_ID_A)) {
            setMenuForID(R.id.action_user_a);
        } else if (userID != null && userID.equals(UserSettingsManager.USER_ID_B)) {
            setMenuForID(R.id.action_user_b);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return setMenuForID(item.getItemId());
    }

    private boolean setMenuForID(int id) {
        firstLaunch = false;
        switch (id) {
            case R.id.action_user_a:
                UserSettingsManager.setUserID(UserSettingsManager.USER_ID_A);
                item1.setTitle("USER A **SELECTED");
                item2.setTitle("USER B");
                updateUI();
                return true;

            case R.id.action_user_b:
                UserSettingsManager.setUserID(UserSettingsManager.USER_ID_B);
                item1.setTitle("USER A");
                item2.setTitle("USER B **SELECTED");
                updateUI();
                return true;

            case R.id.action_config_host:
                ServerDetails.showHostNameDialoge(this);
                return true;

            case R.id.action_config_port:
                ServerDetails.showPortNumberDialog(this);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void socketHasClosed() {
        updateUI();
    }
}
