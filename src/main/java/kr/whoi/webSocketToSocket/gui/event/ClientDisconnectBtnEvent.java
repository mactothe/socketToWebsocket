package kr.whoi.webSocketToSocket.gui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static kr.whoi.webSocketToSocket.handler.WebSocketClientHandler.CONNECT_THREAD;

public class ClientDisconnectBtnEvent implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (CONNECT_THREAD != null && !CONNECT_THREAD.isInterrupted()) {
            CONNECT_THREAD.interrupt();
            CONNECT_THREAD.run();
        }
    }
}
