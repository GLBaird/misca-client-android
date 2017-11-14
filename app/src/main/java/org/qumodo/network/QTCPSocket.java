package org.qumodo.network;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class QTCPSocket {
    private static final String LOG_TAG = "QTCPSocket";
    private String hostname;
    private int port;
    private QClientSocketListener listener;
    private Socket socket;

    private OutputStream os;
    private BufferedReader is;

    public QTCPSocket() {
        this.hostname = "localhost";
        this.port = 3000;
    }

    public QTCPSocket(String hostname, int port, QClientSocketListener listener) {
        this.hostname = hostname;
        this.port = port;
        this.listener = listener;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setBufferedStreams(DataOutputStream os, BufferedReader is) {
        this.os = os;
        this.is = is;
    }

    public void setListener(QClientSocketListener listener) {
        this.listener = listener;
    }

    public void makeConnection() {
        Log.d(LOG_TAG, "Making connection");
        try {
            InetAddress serverAddress = InetAddress.getByName(hostname);
            Log.d(LOG_TAG, "PORT: " + port);
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, port), 2000);
            setupBuffers();
            listener.socketConnectionEstablished();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Socket IO error");
            e.printStackTrace();
            listener.SocketConnectionError(e);
            listener.socketClosed();
        }
    }


    private void setupBuffers() throws IOException {
        Log.d(LOG_TAG, "Creating input and output buffers");
        os = socket.getOutputStream();
        is = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
    }

    public void sendMessage(String message) {
        SendSocketData sender = new SendSocketData();
        sender.execute(message);
    }

    private class SendSocketData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... messages) {
            for (String message : messages) {
                try {
                    os.write(message.getBytes("utf-8"));
                    os.flush();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error sending message from socket");
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private int readErrors = 0;

    public void readInputStream() {
        try {
            String message = null;
            if (is != null)
                message = is.readLine();
            if (message != null) {
                Log.d(LOG_TAG, "Socket data: " + message);
                listener.socketData(message);
            } else if (is != null && is.read() == -1) {
                listener.socketClosed();
                closeSocket();
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error reading from socket");
            e.printStackTrace();
            if (++readErrors >= 5) {
                listener.SocketConnectionError(new Exception("Socket connection has failed"));
                closeSocket();
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    private void teardownSocket() {
        socket = null;
        is = null;
        os = null;
        readErrors = 0;
    }

    public void closeSocket() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                teardownSocket();
                listener.socketClosed();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to close socket");
                e.printStackTrace();
            }
        }
    }


}
