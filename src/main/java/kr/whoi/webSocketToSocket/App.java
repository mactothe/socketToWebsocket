package kr.whoi.webSocketToSocket;

import com.formdev.flatlaf.FlatDarculaLaf;
import kr.whoi.webSocketToSocket.gui.view.MainFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class App extends SpringBootServletInitializer implements Runnable {

    public static boolean USED_GUI = false;
    public static ConfigurableApplicationContext SPRING_CONTEXT = null;
    public static String WEBSOCKET_PATH = "/websocket";
    private static String[] MAIN_ARGS;
    public static JFrame MAIN_FRAME;
    public static String MAIN_FRAME_TITLE = "Socket to Websocket";
    public static int SOCKET_BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        MAIN_ARGS = args;

        Optional<String> findGuiModeArg = Arrays.stream(args).filter(arg -> arg.equals("--gui")).findAny();
        USED_GUI = findGuiModeArg.isPresent();

        Optional<String> findWebSocketPathArg = Arrays.stream(args).filter(arg -> arg.indexOf("--server.websocket-path=") > -1).findAny();

        if (findWebSocketPathArg.isPresent()) {
            String[] argSplit = findWebSocketPathArg.get().split("=");
            if (argSplit.length > 0) {
                WEBSOCKET_PATH = argSplit[1];
            }
        }

        if (USED_GUI) {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, App.class.getResourceAsStream("/font/JetBrainsMono/JetBrainsMono-Bold.ttf")));
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MAIN_FRAME = new JFrame(MAIN_FRAME_TITLE);

            URL imgURL = App.class.getResource("/icon/connector_icon_64.png");
            if (imgURL != null) {
                ImageIcon imageIcon = new ImageIcon(imgURL);
                MAIN_FRAME.setIconImage(imageIcon.getImage());

                if (System.getProperty("os.name").contains("OS X")) {
                    final Taskbar taskbar = Taskbar.getTaskbar();
                    taskbar.setIconImage(imageIcon.getImage());
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    System.setProperty("com.apple.mrj.application.apple.menu.about.name", MAIN_FRAME_TITLE);
                }

                if (SystemTray.isSupported()) {
                    SystemTray systemTray = SystemTray.getSystemTray();
                    PopupMenu trayPopupMenu = new PopupMenu();

                    MenuItem close = new MenuItem("Quit Socket to Web Socket ");
                    close.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.exit(0);
                        }
                    });
                    trayPopupMenu.add(close);

                    TrayIcon trayIcon = new TrayIcon(imageIcon.getImage(), MAIN_FRAME_TITLE, trayPopupMenu);
                    trayIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MAIN_FRAME.setVisible(true);
                        }
                    });
                    trayIcon.setImageAutoSize(true);

                    try {
                        systemTray.add(trayIcon);
                    } catch (AWTException awtException) {
                        awtException.printStackTrace();
                    }

                    MAIN_FRAME.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            super.windowClosing(e);
                            Object[] options = {"exit", "hide"};
                            int select = JOptionPane.showOptionDialog(MAIN_FRAME, "Do you want to exit the program now or hide it?", "exit or hide", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                            if (select == 0) {
                                System.exit(0);
                            }
                            if (select == 1) {
                                MAIN_FRAME.setVisible(false);
                            }
                        }
                    });
                }
            }

            FlatDarculaLaf.install();
            SwingUtilities.invokeLater(new App());
        } else {
            START_WAS(null, null);
        }
    }

    public static void START_WAS(String serverPort, String websocketPath) {
        List<String> argList = new ArrayList();
        argList.addAll(Arrays.asList(MAIN_ARGS));

        List<Integer> removeArgList = new ArrayList();

        for (int i=0; i<argList.size(); i++) {
            if (argList.get(i).indexOf("--server.port=") > -1) {
                removeArgList.add(i);
            }
        }

        for (int i : removeArgList) {
            argList.remove(i);
        }

        if (serverPort != null && !serverPort.equals("")) {
            argList.add("--server.port=" + serverPort);
        }

        if (websocketPath != null && !websocketPath.equals("")) {
            WEBSOCKET_PATH = websocketPath;
        }

        MAIN_ARGS = argList.toArray(new String[0]);

        SPRING_CONTEXT = SpringApplication.run(App.class, MAIN_ARGS);
    }

    public static void SHUTDOWN_WAS() {
        if (SPRING_CONTEXT != null) {
            SpringApplication.exit(SPRING_CONTEXT);
            SPRING_CONTEXT.close();
            SPRING_CONTEXT = null;
        }
    }

    @Override
    public void run() {
        new MainFrame();
    }
}
