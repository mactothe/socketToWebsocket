package kr.whoi.webSocketToSocket.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

public class LogOutputStream extends OutputStream {
    private final static int BUFFER_SIZE = 1024*1024;
    private final JTextPane destination;
    private final Color D_Black = new Color(0, 0, 0);
    private final Color D_Red = new Color(240, 82, 79);
    private final Color D_Blue = new Color(57, 147, 212);
    private final Color D_Magenta = new Color(167, 113, 191);
    private final Color D_Green = new Color(92, 150, 44);
    private final Color D_Yellow = new Color(166, 138, 13);
    private final Color D_Cyan = new Color(0, 163, 163);
    private final Color D_White = new Color(187, 187, 187);
    private final Color B_Black = new Color(34, 34, 34);
    private final Color B_Red = new Color(255, 64, 80);
    private final Color B_Blue = new Color(31, 176, 255);
    private final Color B_Magenta = new Color(237, 126, 237);
    private final Color B_Green = new Color(79, 196, 20);
    private final Color B_Yellow = new Color(229, 191, 0);
    private final Color B_Cyan = new Color(0, 229, 229);
    private final Color B_White = new Color(255, 255, 255);
    private final Color cReset = new Color(255, 255, 255);
    private Color colorCurrent = cReset;
    String remaining = "";

    public LogOutputStream(JTextPane destination) {
        if (destination == null)
            throw new IllegalArgumentException("Destination is null");

        this.destination = destination;
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        SwingUtilities.invokeLater(new Runnable ()
        {
            @Override
            public void run()
            {
                appendANSI(new String(buffer, offset, length));
            }
        });
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b},0, 1);
    }


    public void append(Color c, String s) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        int len = destination.getDocument().getLength();
        destination.setCaretPosition(len);
        destination.setCharacterAttributes(aset, false);
        destination.replaceSelection(s);
    }

    public void appendANSI(String s) {
        int aPos = 0;
        int aIndex = 0;
        int mIndex = 0;
        String tmpString = "";
        boolean stillSearching = true;
        String addString = remaining + s;
        remaining = "";

        if (addString.length() > 0) {
            aIndex = addString.indexOf("\u001B");
            if (aIndex == -1) {
                append(colorCurrent, addString);
                return;
            }


            if (aIndex > 0) {
                tmpString = addString.substring(0, aIndex);
                append(colorCurrent, tmpString);
                aPos = aIndex;
            }

            stillSearching = true;
            while (stillSearching) {
                mIndex = addString.indexOf("m", aPos);
                if (mIndex < 0) {
                    remaining = addString.substring(aPos, addString.length());
                    stillSearching = false;
                    continue;
                } else {
                    tmpString = addString.substring(aPos, mIndex + 1);
                    colorCurrent = getANSIColor(tmpString);
                }
                aPos = mIndex + 1;

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) {
                    tmpString = addString.substring(aPos, addString.length());
                    append(colorCurrent, tmpString);
                    stillSearching = false;
                    continue;
                }

                tmpString = addString.substring(aPos, aIndex);
                aPos = aIndex;
                append(colorCurrent, tmpString);

            }
        }
    }

    public Color getANSIColor(String ANSIColor) {
        if (ANSIColor.equals("\u001B[30m")) {
            return D_Black;
        } else if (ANSIColor.equals("\u001B[31m")) {
            return D_Red;
        } else if (ANSIColor.equals("\u001B[32m")) {
            return D_Green;
        } else if (ANSIColor.equals("\u001B[33m")) {
            return D_Yellow;
        } else if (ANSIColor.equals("\u001B[34m")) {
            return D_Blue;
        } else if (ANSIColor.equals("\u001B[35m")) {
            return D_Magenta;
        } else if (ANSIColor.equals("\u001B[36m")) {
            return D_Cyan;
        } else if (ANSIColor.equals("\u001B[37m")) {
            return D_White;
        } else if (ANSIColor.equals("\u001B[0;30m")) {
            return D_Black;
        } else if (ANSIColor.equals("\u001B[0;31m")) {
            return D_Red;
        } else if (ANSIColor.equals("\u001B[0;32m")) {
            return D_Green;
        } else if (ANSIColor.equals("\u001B[0;33m")) {
            return D_Yellow;
        } else if (ANSIColor.equals("\u001B[0;34m")) {
            return D_Blue;
        } else if (ANSIColor.equals("\u001B[0;35m")) {
            return D_Magenta;
        } else if (ANSIColor.equals("\u001B[0;36m")) {
            return D_Cyan;
        } else if (ANSIColor.equals("\u001B[0;37m")) {
            return D_White;
        } else if (ANSIColor.equals("\u001B[1;30m")) {
            return B_Black;
        } else if (ANSIColor.equals("\u001B[1;31m")) {
            return B_Red;
        } else if (ANSIColor.equals("\u001B[1;32m")) {
            return B_Green;
        } else if (ANSIColor.equals("\u001B[1;33m")) {
            return B_Yellow;
        } else if (ANSIColor.equals("\u001B[1;34m")) {
            return B_Blue;
        } else if (ANSIColor.equals("\u001B[1;35m")) {
            return B_Magenta;
        } else if (ANSIColor.equals("\u001B[1;36m")) {
            return B_Cyan;
        } else if (ANSIColor.equals("\u001B[1;37m")) {
            return D_White;
        } else if (ANSIColor.equals("\u001B[0m")) {
            return cReset;
        } else {
            return D_White;
        }
    }

    public void clampBuffer(int incomingDataSize) {
        Document doc = destination.getStyledDocument();
        int overLength = doc.getLength() + incomingDataSize - BUFFER_SIZE;

        if (overLength > 0)
        {
            try {
                doc.remove(0, overLength);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}
