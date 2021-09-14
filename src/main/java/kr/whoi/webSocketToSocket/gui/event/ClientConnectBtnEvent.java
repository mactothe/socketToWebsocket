package kr.whoi.webSocketToSocket.gui.event;

import kr.whoi.webSocketToSocket.App;
import kr.whoi.webSocketToSocket.handler.WebSocketClientHandler;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import static kr.whoi.webSocketToSocket.App.MAIN_FRAME;
import static kr.whoi.webSocketToSocket.App.MAIN_FRAME_TITLE;

public class ClientConnectBtnEvent implements ActionListener {

    JTextField hostField;
    JTextField bindHostField;
    JTextField hostPortField;
    JTextField localPortField;
    JCheckBox clientLogCheckBox;
    JButton connectBtn;
    JButton disconnectBtn;
    JScrollPane clientLogScrollPanel;

    public ClientConnectBtnEvent(JTextField hostField, JTextField bindHostField, JTextField hostPortField, JTextField localPortField, JCheckBox clientLogCheckBox, JButton connectBtn, JButton disconnectBtn, JScrollPane clientLogScrollPanel) {
        this.hostField = hostField;
        this.bindHostField = bindHostField;
        this.hostPortField = hostPortField;
        this.localPortField = localPortField;
        this.clientLogCheckBox = clientLogCheckBox;
        this.connectBtn = connectBtn;
        this.disconnectBtn = disconnectBtn;
        this.clientLogScrollPanel = clientLogScrollPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        connectBtn = (JButton) e.getSource();
        boolean run = true;
        String dialog = "";
        if (hostField.getText().equals("")) {
            run = false;
            dialog += "- Host Field Input required \n";
        }
        if (hostPortField.getText().equals("")) {
            run = false;
            dialog += "- Local port Field Input required \n";
        }
        if (!dialog.equals("")) {
            dialog = "Please Input!\n" + dialog;
        }
        if (localPortField.getText().equals("")) {
            dialog += "If there is no input value for Bind Port field, the default port is [52525].\n";
        }
        if (!dialog.equals("")) {
            JOptionPane.showMessageDialog(App.MAIN_FRAME, dialog, "Warring", JOptionPane.ERROR_MESSAGE);
        }

        if (run) {
            String host = hostField.getText();
            String bindHost = bindHostField.getText();
            String hostPort = hostPortField.getText();
            String localPort = localPortField.getText();

            try {
                WebSocketClientHandler webSocketClientHandler = new WebSocketClientHandler(host, bindHost, hostPort, localPort) {
                    @Override
                    public void connectArise() {
                        if (SystemTray.isSupported()) {
                            SystemTray systemTray = SystemTray.getSystemTray();
                            TrayIcon[] trayIcons = systemTray.getTrayIcons();
                            trayIcons[0].setToolTip(MAIN_FRAME_TITLE);
                        }
                        MAIN_FRAME.setTitle(MAIN_FRAME_TITLE);
                        hostField.setEnabled(false);
                        bindHostField.setEnabled(false);
                        hostPortField.setEnabled(false);
                        localPortField.setEnabled(false);
                        connectBtn.setEnabled(false);
                        disconnectBtn.setEnabled(true);

                    }

                    @Override
                    public void disconnectArise() {
                        if (SystemTray.isSupported()) {
                            SystemTray systemTray = SystemTray.getSystemTray();
                            TrayIcon[] trayIcons = systemTray.getTrayIcons();
                            trayIcons[0].setToolTip(MAIN_FRAME_TITLE);
                        }
                        MAIN_FRAME.setTitle(MAIN_FRAME_TITLE);
                        hostField.setEnabled(true);
                        bindHostField.setEnabled(true);
                        hostPortField.setEnabled(true);
                        localPortField.setEnabled(true);
                        connectBtn.setEnabled(true);
                        disconnectBtn.setEnabled(false);

                    }

                    @Override
                    public void socketLog(String log) {
                        JTextPane clientLogTextPanel = (JTextPane) ((JViewport) clientLogScrollPanel.getComponent(0)).getComponent(0);
                        StyledDocument document = (StyledDocument) clientLogTextPanel.getDocument();
                        if (clientLogCheckBox.isSelected()) {
                            try {
                                document.insertString(document.getLength(), log, null);
                                clientLogScrollPanel.getVerticalScrollBar().setValue(clientLogScrollPanel.getVerticalScrollBar().getMaximum());
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
            }
        }

    }
}
