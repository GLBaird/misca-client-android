package org.qumodo.network;

public interface QClientSocketListener {

    public void socketConnectionEstablished();
    public void SocketConnectionError(Exception error);
    public void socketClosed();
    public void socketData(String data);

}
