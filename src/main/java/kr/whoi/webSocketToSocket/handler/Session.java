package kr.whoi.webSocketToSocket.handler;

import java.io.Serializable;
import java.util.Arrays;

public class Session implements Serializable {
    private static final long serialVersionUID = 1934923432215186136L;

    private String connectionId;
    private String message;
    private int bufferSize;
    private byte[] buf;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Session(String connectionId, int bufferSize) {
        this.connectionId = connectionId;
        this.bufferSize = bufferSize;
    }

    public Session(String connectionId, int bufferSize, String message) {
        this.connectionId = connectionId;
        this.bufferSize = bufferSize;
        this.message = message;
    }

    public Session(String connectionId, int bufferSize, byte[] buf) {
        this.connectionId = connectionId;
        this.bufferSize = bufferSize;
        this.buf = buf;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public String toString() {
        return "Session{" +
                "connectionId='" + connectionId + '\'' +
                ", message='" + message + '\'' +
                ", bufferSize=" + bufferSize +
                ", buf=" + Arrays.toString(buf) +
                '}';
    }
}