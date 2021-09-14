package kr.whoi.webSocketToSocket.handler;

import kr.whoi.webSocketToSocket.util.WebSocketClientUtil;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class WebSocketClientHandler {

    String host;
    String bindHost;
    String hostPort;
    public static Thread CONNECT_THREAD;
    private static int CONNECT_THREAD_COUNT = 0;

    public WebSocketClientHandler(String host, String bindHost, String hostPort, String localPort) throws IOException, URISyntaxException {
        this.host = host;
        this.bindHost = bindHost;
        this.hostPort = hostPort;

        int serverSocketPort = Integer.parseInt(localPort);
        ServerSocket serverSocket = new ServerSocket();
        InetSocketAddress sockAddr = new InetSocketAddress("127.0.0.1", serverSocketPort);
        serverSocket.bind(sockAddr);

        final Map<String, String> requestHeader = new HashMap();
        requestHeader.put("bind-host", bindHost);
        requestHeader.put("host-port", hostPort);
        URI uri = new URI(host);

        CONNECT_THREAD = webClientConnect(uri, requestHeader, serverSocket);

        if (CONNECT_THREAD != null) {
            CONNECT_THREAD.start();
            connectArise();
        }
    }

    private Thread webClientConnect(final URI uri, final Map<String, String> requestHeader, final ServerSocket serverSocket) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (!CONNECT_THREAD.isInterrupted()) {
                    final WebSocketClientUtil.SocketWorker worker = new WebSocketClientUtil.SocketWorker();
                    final Thread workerThread = new Thread(worker);
                    try {
                        worker.clientSocket = serverSocket.accept();
                        worker.socketUtil = new WebSocketClientUtil(uri, requestHeader) {
                            @Override
                            public void onOpen(ServerHandshake serverHandshake) {
                                CONNECT_THREAD_COUNT++;
                                socketLog("client thread count: " + CONNECT_THREAD_COUNT);
                            }

                            @Override
                            public void onMessage(String message) {
                                synchronized (this) {
                                    if (message.equals("CLOSE_SOCKET")) {
                                        close();
                                    }
                                    socketLog(message + "\n");
                                }
                            }

                            @Override
                            public void onMessageFromSocket(ByteBuffer message) {
                                synchronized (this) {
                                    if (!isClosed()) {
                                        socketLog("Receive from socket " + message.array().length + "byte(s)\n");
                                        send(message);
                                        socketLog("Send to websocket  " + message.array().length + "byte(s)\n");
                                    }
                                }
                            }

                            @Override
                            public void onMessage(ByteBuffer message) {
                                synchronized (this) {
                                    try {
                                        socketLog("Receive from websocket " + message.array().length + "byte(s)\n");
                                        worker.writer.write(message.array(), message.arrayOffset(), message.array().length);
                                        worker.writer.flush();
                                        socketLog("Send to socket " + message.array().length + "byte(s)\n");
                                    } catch (IOException e) {
                                        socketLog(e.getMessage());
                                    }
                                }
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {
                                synchronized (this) {
                                    CONNECT_THREAD_COUNT--;
                                    socketLog("client thread count: " + CONNECT_THREAD_COUNT);
                                    send("CLOSE_SOCKET");
                                    try {
                                        if (worker.reader != null) {
                                            worker.reader.close();
                                        }

                                        if (worker.writer != null) {
                                            worker.writer.close();
                                        }

                                        if (worker.clientSocket != null && !worker.clientSocket.isClosed()) {
                                            worker.clientSocket.close();
                                        }
                                    } catch (IOException e) {
                                        socketLog(e.getMessage());
                                    }
                                    super.close();
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                socketLog(e.getMessage());
                            }

                            @Override
                            public void onSocketClose() {
                                synchronized (this) {
                                    try {
                                        if (worker.reader != null) {
                                            worker.reader.close();
                                        }

                                        if (worker.writer != null) {
                                            worker.writer.close();
                                        }

                                        if (worker.clientSocket != null && !worker.clientSocket.isClosed()) {
                                            worker.clientSocket.close();
                                        }
                                    } catch (IOException e) {
                                        socketLog(e.getMessage());
                                    }
                                }
                                this.close();
                            }
                        };
                        worker.socketUtil.connect();
                    } catch (SocketException e) {
                        socketLog(e.getMessage());
                        break;
                    } catch (IOException e) {
                        socketLog(e.getMessage());
                    }
                }
                disconnectArise();
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    socketLog(e.getMessage());
                }
            }
        });
    }

    public abstract void connectArise();
    public abstract void disconnectArise();
    public abstract void socketLog(String log);

}
