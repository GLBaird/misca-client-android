package org.qumodo.miscaclient.dataProviders;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.qumodo.miscaclient.R;

public class ServerDetails {

    private static final String SOCKET_HOSTNAME_KEY = "org.miscaclient.ServerDetails.SocketHostNameKey";
    private static final String MEDIA_SERVER_HOSTNAME = "org.miscaclient.ServerDetails.MediaServerHostKey";
    private static final String SOCKET_PORT_NUMBER = "org.miscaclient.ServerDetails.SocketPortNumber";

    public static class SocketCommands {
        public static final String CORE_IMAGE_SEARCH = "core_image_search";
        public static final String MISCA_OBJECT_SEARCH = "misca_object_search";
        public static final String MISCA_ANPR_SEARCH = "misca_anpr_search";
        public static final String MISCA_ANPR_SEARCH_EXTRA = "misca_anpr_search_extra";
    }

    public static void showHostNameDialoge(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_hostname, null);
        final EditText ipAddress = dialogView.findViewById(R.id.ip_address);
        ipAddress.setText(getSocketHostName());
        builder.setTitle("Server Configuration")
                .setView(dialogView)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String hostName = ipAddress.getText().toString();
                        UserSettingsManager.setValue(SOCKET_HOSTNAME_KEY, hostName);
                        UserSettingsManager.setValue(MEDIA_SERVER_HOSTNAME, "http://"+hostName);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
    }

    public static void showPortNumberDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_port, null);
        final EditText portNumber = dialogView.findViewById(R.id.port_number);
        portNumber.setText(String.valueOf(getSocketPortNumber()));
        builder.setTitle("Server Configuration")
                .setView(dialogView)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int port = Integer.parseInt(portNumber.getText().toString());
                        UserSettingsManager.setValue(SOCKET_PORT_NUMBER, port);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
    }

    public static String getSocketHostName() {
        return UserSettingsManager.getValue(SOCKET_HOSTNAME_KEY, "192.168.1.110");
    }

    public static int getSocketPortNumber() {
        return UserSettingsManager.getValue(SOCKET_PORT_NUMBER, 9500);
    }

    public static String getMediaServerHostName() {
        return UserSettingsManager.getValue(MEDIA_SERVER_HOSTNAME, "http://192.168.1.110");
    }

    public static String getUserMessageImageHostName(String messageID) {
        return getMediaServerHostName() + ":9800/message_image/"+messageID;
    }

    public static String getMiscaImageHostName(String image) {
        return UserSettingsManager.getValue(MEDIA_SERVER_HOSTNAME, "http://192.168.1.110")
                + ":9800/misca_image/"+image;
    }

}
