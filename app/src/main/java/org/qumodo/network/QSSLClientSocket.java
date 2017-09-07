package org.qumodo.network;

import android.content.Context;
import android.util.Log;

import org.qumodo.miscaclient.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class QSSLClientSocket {

    private static final String LOG_TAG = "SSL_CLIENT_SOCKET";

    private int port = 9500;
    private String hostname = "misca.qumo.do";

    private Context appContext;
    private SSLSocket socket;
    private SSLSession session;
    public BufferedWriter os;
    public BufferedReader is;
    private Certificate[] serverCerts;
    private QClientSocketListener listener;

    public QSSLClientSocket(Context appContext) {
        this.appContext = appContext;
    }

    public QSSLClientSocket(Context appContext, QClientSocketListener listener) {
        this.appContext = appContext;
        this.listener = listener;
    }

    public QSSLClientSocket(Context appContext, QClientSocketListener listener, SSLSocket socket) {
        this.appContext = appContext;
        this.listener = listener;
        this.socket = socket;
    }

    public void setHostNameAndPort(String hostName, int port) {
        this.hostname = hostName;
        this.port = port;
    }

    public void setListener(QClientSocketListener listener) {
        this.listener = listener;
    }


    private KeyStore getKeyStore() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        // load keystore and setup self-signed certificate authority
        final KeyStore ks = KeyStore.getInstance("BKS");
        final InputStream inputStream = this.appContext.getResources().openRawResource(R.raw.key_store);
        ks.load(inputStream, this.appContext.getString(R.string.store_pass).toCharArray());
        inputStream.close();
        return ks;
    }

    private Certificate loadCertificate() throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(this.appContext.getResources().openRawResource(R.raw.certificate));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }
        return ca;
    }

    private KeyStore getCertKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        Certificate cert = loadCertificate();
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("ca", cert);
        return ks;
    }

    private TrustManager[] getTrustManagers(KeyStore ks) throws NoSuchAlgorithmException, KeyStoreException {
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(ks);
        return tmf.getTrustManagers();
    }

    private SSLSocket makeSocket() throws NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException, KeyStoreException {
        // assign CA (Self Signed)
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, getTrustManagers(getKeyStore()), null);
        SSLSocketFactory factory = context.getSocketFactory();
        return  (SSLSocket) factory.createSocket(hostname, port);
    }

    private SSLSocket makeSocket2() throws IOException {
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port);
        return socket;
    }

    public void makeConnection() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {

        Log.d(LOG_TAG, String.format("Starting SSL Connection to %s, %d", hostname, port));

        if (listener == null) {
            throw new NullPointerException("Need to set a listener for socket");
        }

        if (socket == null) {
            Log.d(LOG_TAG, "Creating new socket");
            socket = makeSocket2();
            Log.d(LOG_TAG, "Socket Completed");
        }

        socket.setEnableSessionCreation(true);
        socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
            @Override
            public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
                Log.d(LOG_TAG, "Handshake complete");
                session = handshakeCompletedEvent.getSession();
                try {
                    serverCerts = session.getPeerCertificates();
                    logServerCertificates();

                    // setup streams
                    os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Confirm socket to delegate
                    Log.d(LOG_TAG, "About to confirm handshake and IO Buffered");
                    listener.socketConnectionEstablished();
                    Log.d(LOG_TAG, "First Read");
                    readSocketData();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Peer Unverified Error!");
                    e.printStackTrace();
                    listener.SocketConnectionError(e);
                }
            }
        });

        Log.d(LOG_TAG, "Start handshake");
        socket.startHandshake();
        Log.d(LOG_TAG, "Handshake complete");
    }

    private void logServerCertificates() {
        Log.d(LOG_TAG, serverCerts.length + "Certificates Found");
        for (int i = 0; i < serverCerts.length; i++) {
            Certificate myCert = serverCerts[i];
            Log.d(LOG_TAG, "====Certificate:" + (i + 1) + "====");
            Log.d(LOG_TAG, "-Public Key-n" + myCert.getPublicKey());
            Log.d(LOG_TAG, "-Certificate Type-n " + myCert.getType());
        }
    }

    public Certificate[] getCertificstaticates() {
        return serverCerts;
    }

    public Boolean isConnected() {
        return socket.isConnected();
    }

    public Boolean isClosed() {
        return socket.isClosed();
    }

    public void sendData(String data) throws IOException {
        if (isConnected()) {
            os.write(data);
            os.flush();
        } else {
            throw new IOException("Can't send data, socket is not connected");
        }
    }

    public void closeSocket() throws IOException {
        os.close();
        is.close();
        socket.close();
        Log.d(LOG_TAG, "Socket has been closed");
        this.tearDownSocket();
        listener.socketClosed();
    }

    private void tearDownSocket() {
        socket = null;
        session = null;
        serverCerts = null;
        os = null;
        is = null;
    }

    private void readSocketData() {
        Log.d(LOG_TAG, "Reading data");
        Boolean socketClosed = false;
        try {
            String data = is.readLine();
            socketClosed = !data.equals("-1");
            Log.d(LOG_TAG, "Data Received: " + data);
            if (isConnected() && !socketClosed) {
                listener.socketData(data);
                this.readSocketData();
            } else if (socketClosed || isClosed() || !isConnected()) {
                this.closeSocket();
                listener.socketClosed();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading data");
            e.printStackTrace();
            if (socketClosed) {
                tearDownSocket();
            }
        }
    }

}