package kr.whoi.webSocketToSocket.util;

import kr.whoi.webSocketToSocket.handler.Session;
import org.java_websocket.client.WebSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

import static kr.whoi.webSocketToSocket.App.SOCKET_BUFFER_SIZE;

public abstract class WebSocketClientUtil extends WebSocketClient {
    public WebSocketClientUtil(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void run() {
        super.run();
    }

    public abstract void onMessageFromSocket(ByteBuffer message);

    public abstract void onSocketClose();

    public static class SocketWorker implements Runnable {
        public InputStream reader;
        public OutputStream writer;
        public Session session;
        public WebSocketClientUtil socketUtil;
        public Socket clientSocket;
        public boolean loop = true;

        @Override
        public void run() {
            try {
                while (loop) {
                    byte[] buffer = new byte[SOCKET_BUFFER_SIZE];
                    int recvLength = this.reader.read(buffer);

                    if (recvLength == 0) {
                        break;
                    }

                    if (recvLength == -1) {
                        break;
                    }

                    byte[] buf = new byte[recvLength];

                    System.arraycopy(buffer, 0, buf, 0, recvLength);
                    socketUtil.onMessageFromSocket(ByteBuffer.wrap(buf));
                }
                socketUtil.onSocketClose();
            } catch (IOException e) {
                socketUtil.onSocketClose();
            }
        }

        public void stop () throws IOException {
            loop = false;
            clientSocket.close();
        }
    }

}
