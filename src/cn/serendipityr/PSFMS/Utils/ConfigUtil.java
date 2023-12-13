package cn.serendipityr.PSFMS.Utils;

import cc.summermc.bukkitYaml.InvalidConfigurationException;
import cc.summermc.bukkitYaml.file.YamlConfiguration;
import cn.serendipityr.PSFMS.PortScannerForMinecraftServer;

import java.util.List;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static cn.serendipityr.PSFMS.PortScannerForMinecraftServer.config;
import static cn.serendipityr.PSFMS.PortScannerForMinecraftServer.configFile;

public class ConfigUtil {
    public static List<String> ScanHostAddress;
    public static String HostAddress;
    public static String ScanAddress;
    public static Long ScanDelay;
    public static Integer ScanThreads;
    public static Integer AddressThreads;
    public static File OutputFile;
    public static Integer ConnectTimeout;
    public static Integer ReadTimeout;
    public static Integer MinPort;
    public static Integer MaxPort;
    public static Boolean ShowFails;
    public static Boolean ShowStats;

    public static Boolean LogCurrentIP;
    public static Boolean LogTCP;
    public static Boolean LogMinecraft;
    public static Boolean LogKonterStriker;
    public static Boolean LogHTTP;
    public static Boolean LogPlayerList;
    public static Boolean LogVersion;

    public static Integer VersionProtocol;
    public static List<String> MotdSearch;
    public static List<String> ModsSearch;
    public static List<String> VersionSearch;
    public static List<String> TitleSearch;
    public static List<String> TitleExclude;

    public void loadConfig() {
        try {
            ScanHostAddress = config.getStringList("ScanHostAddress");
            ScanDelay = config.getLong("ScanDelay");
            ScanThreads = config.getInt("ScanThreads");
            AddressThreads = config.getInt("AddressThreads");
            ConnectTimeout = config.getInt("ConnectTimeout");
            ReadTimeout = config.getInt("ReadTimeout");
            MinPort = config.getInt("MinPort");
            MaxPort = config.getInt("MaxPort");
            ShowFails = config.getBoolean("ShowFails");
            ShowStats = config.getBoolean("ShowStats");


            LogCurrentIP = config.getBoolean("LogCurrentIP");
            LogTCP = config.getBoolean("LogTCP");
            LogMinecraft = config.getBoolean("LogMinecraft");
            LogKonterStriker = config.getBoolean("LogKonterStriker");
            LogHTTP = config.getBoolean("LogHTTP");
            LogPlayerList = config.getBoolean("LogPlayerList");
            LogVersion = config.getBoolean("LogVersion");

            MotdSearch = config.getStringList("MotdSearch");
            ModsSearch = config.getStringList("ModsSearch");
            VersionSearch = config.getStringList("VersionSearch");
            TitleSearch = config.getStringList("TitleSearch");
            TitleExclude = config.getStringList("TitleExclude");
            VersionProtocol = config.getInt("VersionProtocol");

            try {
                ScanAddress = toIPAddress(HostAddress);
            } catch (Exception ignored) {}

            if (ScanAddress == null) {
                LogUtil.emptyLog();
                LogUtil.doLog(1, "Вы ввели неверный адрес. Повторите попытку.", "CFGUtil");
                loadConfig();
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fileName = config.getString("OutputFile");
            OutputFile = new File(fileName.replaceAll("%address%", ScanHostAddress.toArray().toString()).replaceAll("%time%", simpleDateFormat.format(new Date())));

            LogUtil.emptyLog();
            LogUtil.doLog(0, "==============================================================", "CFGUtil");
            LogUtil.doLog(0, "Адрес: " + ScanHostAddress, "CFGUtil");
            LogUtil.doLog(0, "Порты: " + MinPort + "-" + MaxPort, "CFGUtil");
            LogUtil.doLog(0, "Потоки: " + ScanThreads+" | "+AddressThreads, "CFGUtil");
            LogUtil.doLog(0, "Интервал: " + ScanDelay + " мс", "CFGUtil");
            LogUtil.doLog(0, "Таймаут: " + ConnectTimeout + " | " + ReadTimeout + " мс", "CFGUtil");
            LogUtil.doLog(0, "Результат: " + OutputFile.getName(), "CFGUtil");
            LogUtil.doLog(0, "==============================================================", "CFGUtil");
            LogUtil.emptyLog();
        } catch (Exception e) {
            LogUtil.emptyLog();
            LogUtil.doLog(1, "Не удалось загрузить файл конфигурации! Подробности: " + e, null);
            LogUtil.doLog(-1, "В конфигурации может быть проблема с кодировкой. Пробовали ли вы преобразовать кодировку, чтобы решить эту проблему? [y/n]:", "CFGUtil");
            Scanner scanner = new Scanner(System.in);
            if (scanner.nextLine().contains("y")) {
                String currentCharset = getFileCharset(configFile);

                File tempConfigFile = new File("config_temp.yml");

                convertFileCharset(configFile, tempConfigFile, currentCharset, "UTF-8");

                if (configFile.delete()) {
                    tempConfigFile.renameTo(configFile);
                }

                LogUtil.doLog(0, "Миссия выполнена. Кодирование перед преобразованием:" + currentCharset + " | Преобразованная кодировка: " + getFileCharset(configFile) , "CFGUtil");
                LogUtil.emptyLog();
            }

            loadConfig();
        }
    }

    public static String getFileCharset(File file) {
        String charset = "UTF-8";

        byte[] first3Bytes = new byte[3];

        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()));
            bis.mark(100);

            int read = bis.read(first3Bytes, 0, 3);

            if (read == -1) {
                bis.close();
                return charset; // 文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; // 文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; // 文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; // 文件编码为 UTF-8
                checked = true;
            }

            bis.reset();

            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (!(0x80 <= read && read <= 0xBF)) {
                            break;
                        }
                    } else if (0xE0 <= read) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                            }
                        }
                        break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return charset;
    }

    public static void convertFileCharset(File inputFile, File outputFile,String currentCharset ,String targetCharset) {
        try {
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(inputFile.toPath()) ,currentCharset);
            java.io.OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(outputFile.toPath()) ,targetCharset);

            int len;
            while((len = isr.read())!=-1){
                osw.write(len);
            }

            osw.close();
            isr.close();
        } catch (Exception e) {
            LogUtil.doLog(1, "Произошла ошибка при преобразовании кодировки файла! Подробности: " + e, null);
            System.exit(0);
        }
    }

    public static String toIPAddress(String input) {
        try {
            // 尝试将输入字符串解析为IP地址
            InetAddress addr = InetAddress.getByName(input);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            // 如果输入字符串无法解析为IP地址，则返回null
            return null;
        }
    }
}
