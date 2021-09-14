package kr.whoi.webSocketToSocket.gui.event;

import kr.whoi.webSocketToSocket.App;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class ServerStopBtnEvent implements ActionListener {
    private JButton serverStartBtn;
    private JButton serverStopBtn;

    public ServerStopBtnEvent(JButton serverStartBtn, JButton serverStopBtn) {
        this.serverStartBtn = serverStartBtn;
        this.serverStopBtn = serverStopBtn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        App.SHUTDOWN_WAS();
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        serverStartBtn.setEnabled(true);
        serverStopBtn.setEnabled(false);
    }
}
