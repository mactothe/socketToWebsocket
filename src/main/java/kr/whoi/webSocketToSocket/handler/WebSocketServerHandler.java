package kr.whoi.webSocketToSocket.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static kr.whoi.webSocketToSocket.App.SOCKET_BUFFER_SIZE;

@Component
public class WebSocketServerHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(WebSocketServerHandler.class);
    private static int threadCount = 0;
    private Map<WebSocketSession, Map<String, Object>> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        synchronized (logger) {
            session.setBinaryMessageSizeLimit(SOCKET_BUFFER_SIZE);
            session.setTextMessageSizeLimit(SOCKET_BUFFER_SIZE);

            sessions.put(session, new HashMap());
            Map sessionInfo = sessions.get(session);

            String bindHost = session.getHandshakeHeaders().get("bind-host").get(0);
            String localPort = session.getHandshakeHeaders().get("host-port").get(0);
            bindHost = bindHost.equals("")?"localhost":bindHost;
            sessionInfo.put("authentication", true);
            if (localPort == null || localPort.equals("")) {
                session.sendMessage(new TextMessage("Empty local port!"));
                session.sendMessage(new TextMessage("Session Closed."));
                session.close();
            } else {
                SocketWorker socketWorker = new SocketWorker();
                socketWorker.port = Integer.parseInt(localPort);
                socketWorker.address = bindHost;
                session.sendMessage(new TextMessage("SUCCESS_CONNECT"));
                session.sendMessage(new TextMessage("new Websocket Server Connect!\n"));
                logger.debug("[" + session.getId() + "] connect to " + socketWorker.address + ":" + socketWorker.port);
                socketWorker.clientSocket = new Socket(socketWorker.address, socketWorker.port);
                socketWorker.reader = socketWorker.clientSocket.getInputStream();
                socketWorker.writer = socketWorker.clientSocket.getOutputStream();
                socketWorker.session = session;
                (new Thread(socketWorker)).start();
                sessionInfo.put("socketWorker", socketWorker);
                logger.debug("[" + session.getId() + "] ws connected.");
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        synchronized(logger) {
            if (message.getPayload().equals("CLOSE_SOCKET")) {
                if (sessions.get(session) != null) {
                    SocketWorker sessionWorker = (SocketWorker) sessions.get(session).get("worker");
                    try {
                        sessionWorker.stop();
                        String bindHost = session.getHandshakeHeaders().get("bind-host").get(0);
                        String localPort = session.getHandshakeHeaders().get("local-port").get(0);
                        bindHost = bindHost.equals("") ? "localhost" : bindHost;
                        SocketWorker socketWorker = new SocketWorker();
                        socketWorker.port = Integer.parseInt(localPort);
                        socketWorker.address = bindHost;
                        socketWorker.session = session;
                        socketWorker.clientSocket = new Socket(socketWorker.address, socketWorker.port);
                        socketWorker.reader = socketWorker.clientSocket.getInputStream();
                        socketWorker.writer = socketWorker.clientSocket.getOutputStream();
                        sessions.get(session).put("worker", socketWorker);
                        (new Thread(socketWorker)).start();
                        session.sendMessage(new TextMessage("CLOSE_SOCKET"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        synchronized(logger) {
            if (sessions.get(session) != null) {
                SocketWorker socketWorker = (SocketWorker) sessions.get(session).get("worker");
                byte[] buf = message.getPayload().array();
                logger.debug("[server] Receive Client Websocket: " + buf.length + "bytes");
                try {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
                    socketWorker.writer.write(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.array().length);
                    socketWorker.writer.flush();
                    logger.debug("[server] Send Connect socket: " + buf.length + "bytes");
                } catch (SocketException e) {
                    socketWorker.stop();
                }
            }
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized(logger) {
            SocketWorker socketWorker = (SocketWorker) sessions.get(session).get("worker");
            if (socketWorker != null) {
                if (socketWorker.clientSocket != null && !socketWorker.clientSocket.isClosed()) {
                    socketWorker.clientSocket.close();
                }
                sessions.remove(session);
                logger.debug("[" + session.getId() + "] ws closed.");
            }
            super.afterConnectionClosed(session, status);
        }
    }

    private static class SocketWorker implements Runnable {
        public int port;
        public String address;
        public Socket clientSocket;
        public InputStream reader;
        public OutputStream writer;
        public WebSocketSession session;
        public boolean loop = true;

        @Override
        public void run() {
            if (this.session != null) {
                logger.debug("[" + this.session.getId() + "] thread begin.");
                synchronized (logger) {
                    WebSocketServerHandler.threadCount = WebSocketServerHandler.threadCount + 1;
                    logger.debug("[" + this.session.getId() + "] thread count " + WebSocketServerHandler.threadCount);
                }

                try {
                    while (loop) {
                        byte[] buffer = new byte[SOCKET_BUFFER_SIZE];
                        int recvlen = this.reader.read(buffer);
                        if (recvlen == 0) {
                            throw new IOException("force close");
                        }

                        if (recvlen == -1) {
                            throw new IOException("force close");
                        }
                        byte[] buf = new byte[recvlen];

                        for (int i = 0; i < recvlen; ++i) {
                            buf[i] = buffer[i];
                        }
                        logger.debug("[server] Receive Connect Socket: " + buf.length + "bytes");
                        this.session.sendMessage(new BinaryMessage(buf));
                        logger.debug("[server] Send Client Websocket: " + buf.length + "bytes");
                    }
                } catch (IOException e) {
                    logger.debug("[" + this.session.getId() + "] thread excepd " + e.getMessage());
                    synchronized (logger) {
                        WebSocketServerHandler.threadCount = WebSocketServerHandler.threadCount - 1;
                        logger.debug("[" + this.session.getId() + "] thread count " + WebSocketServerHandler.threadCount);
                    }
                    logger.debug("[" + this.session.getId() + "] thread end.");
                }
            }
        }

        public void stop () throws IOException {
            loop = false;
            clientSocket.close();
        }
    }
}
