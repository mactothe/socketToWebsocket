package kr.whoi.webSocketToSocket.gui.event;

import kr.whoi.webSocketToSocket.App;
import kr.whoi.webSocketToSocket.util.LogOutputStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ServerStartBtnEvent implements ActionListener {
    private JButton serverStartBtn;
    private JButton serverStopBtn;
    private JTextField websocketTextField;
    private JFormattedTextField serverPortField;
    private JTextPane serverLogTextPane;

    public ServerStartBtnEvent(JButton serverStartBtn, JButton serverStopBtn, JTextField websocketTextField, JFormattedTextField serverPortField, JTextPane serverLogTextPane) {
        this.serverStartBtn = serverStartBtn;
        this.serverStopBtn = serverStopBtn;
        this.websocketTextField = websocketTextField;
        this.serverPortField = serverPortField;
        this.serverLogTextPane = serverLogTextPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String webSocketPath = websocketTextField.getText();
        String serverPort = serverPortField.getText();
        serverLogTextPane.setText("");
        System.setOut(new PrintStream(new LogOutputStream(serverLogTextPane)));
        App.START_WAS(serverPort, webSocketPath);
        serverStartBtn.setEnabled(false);
        serverStopBtn.setEnabled(true);
    }
}
