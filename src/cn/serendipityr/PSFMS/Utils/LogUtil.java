package cn.serendipityr.PSFMS.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LogUtil {
    public static void doLog(int type, String content, String extra) {
        String logType;
        String msg;

        switch (type) {
            case 1:
                logType = "[Internal Error]";
                break;
            case 2:
                logType = "[DEBUG]";
                break;
            default:
                logType = "[" + extra + "]";
        }

        msg = logType + " " + content;

        if (type == -1) {
            try {
                print(msg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                println(msg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void emptyLog() {
        System.out.println();
    }

    public static void print(String str) throws UnsupportedEncodingException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(str);
    }

    public static void println(String str) throws UnsupportedEncodingException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println(str);
    }

    public static void saveOpeningPort(String content) {
        File resultFile = ConfigUtil.OutputFile;

        try {
            FileWriter fileWriter = new FileWriter(resultFile, true);
            fileWriter.write(content + "\n");
            fileWriter.close();
        } catch (IOException e) {
            LogUtil.doLog(1, "Ошибка при сохранении: " + e.getMessage(), null);
        }
    }
}