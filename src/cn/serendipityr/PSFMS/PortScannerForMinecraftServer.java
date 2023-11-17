package cn.serendipityr.PSFMS;

import cc.summermc.bukkitYaml.file.YamlConfiguration;
import cn.serendipityr.PSFMS.Utils.ConfigUtil;
import cn.serendipityr.PSFMS.Utils.TCPChecker;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static cn.serendipityr.PSFMS.Utils.ConfigUtil.*;

public class PortScannerForMinecraftServer {
    public static final String statsFormat = "[ Tried CPS %s | Average CPS %s |  Current CPS %s ] ( MC %s / TCP %s ) { Time Active: %s | ETA: %s }";

    public static File configFile;
    public static YamlConfiguration config;

    public static volatile int curCPS = 0;
    public static volatile int triedCPS = 1;
    public static volatile int avgCPS = 0;
    public static volatile int totalConnections = 0;
    public static volatile double totalSeconds = 1;
    public static volatile int totalFoundTCP = 0;
    public static volatile int totalFoundMC = 0;

    public static volatile boolean scanning = true;
    public volatile static List<String> allIPs = new ArrayList<>();


    public static void main(String[] args) {
        configFile = new File("config.yml");

        if (!configFile.exists()) {
            try {
                copyResourceToFile("config.yml", configFile);
            } catch (IOException e) {
                System.err.println("Не удалось сохранить config.yml из ресурсов: " + e.getMessage());
                System.exit(1);
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        new ConfigUtil().loadConfig();

        List<String> ipPatterns = config.getStringList("ScanHostAddress");
        for (String ipPattern : ipPatterns) {
            List<String> singleIPs = generateMassiveIPList(ipPattern);
            allIPs.addAll(singleIPs);
        }

        if (ConfigUtil.ShowStats) {
            startCounter();
        }

        TCPChecker checker = new TCPChecker();
        checker.doPortScan(allIPs);

        scanning = false;
        System.out.println("Scanning finished. Press ENTER");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.exit(0);
    }

    private static void startCounter() {
        Thread Counter = new Thread(() -> {
            int delay = ConnectTimeout * 2;
            while (scanning) {
                try {
                    Thread.sleep(delay);

                    totalSeconds += (double) delay / 1000;
                    avgCPS = (int) (totalConnections / totalSeconds);

                    String timeActive = LocalTime.ofSecondOfDay((int) totalSeconds).toString();
                    String timeLeft = LocalTime.ofSecondOfDay((long) allIPs.size() * (MaxPort - MinPort) / triedCPS).toString();

                    String out = String.format(statsFormat, triedCPS, avgCPS, curCPS, totalFoundMC, totalFoundTCP, timeActive, timeLeft);

                    System.out.println(out);

                    PortScannerForMinecraftServer.curCPS = 0;
                    triedCPS = 1;
                } catch (InterruptedException | DateTimeException ignored) {}
            }
        });
        Counter.setPriority(1);
        Counter.start();
    }

    private static List<String> generateMassiveIPList(String pattern) {
        List<String> ipList = new ArrayList<>();

        String[] parts = pattern.split("\\.");

        List<Integer> starIndexes = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("*")) {
                starIndexes.add(i);
            }
        }

        int combinations = 1 << (starIndexes.size() * 8);
        for (int i = 0; i < combinations; i++) {
            StringBuilder ipBuilder = new StringBuilder();

            for (int j = 0; j < parts.length; j++) {
                if (starIndexes.contains(j)) {
                    int octet = (i >> (starIndexes.size() - starIndexes.indexOf(j) - 1) * 8) & 0xFF;
                    ipBuilder.append(octet);
                } else {
                    ipBuilder.append(parts[j]);
                }

                if (j < parts.length - 1) {
                    ipBuilder.append(".");
                }
            }

            ipList.add(ipBuilder.toString());
        }

        return ipList;
    }

    public static void copyResourceToFile(String resourceName, File destinationFile) throws IOException {
        try (InputStream inputStream = PortScannerForMinecraftServer.class.getClassLoader().getResourceAsStream(resourceName);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
 }
