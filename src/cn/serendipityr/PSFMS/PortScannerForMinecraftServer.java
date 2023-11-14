package cn.serendipityr.PSFMS;

import cc.summermc.bukkitYaml.file.YamlConfiguration;
import cn.serendipityr.PSFMS.Utils.ConfigUtil;
import cn.serendipityr.PSFMS.Utils.TCPChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PortScannerForMinecraftServer {
    public static File configFile;
    public static YamlConfiguration config;

    public static void main(String[] args) {
        start();
        System.out.println("Конец.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    public static void start() {
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

        List<String> allIPs = new ArrayList<>();
        for (String ipPattern : ipPatterns) {
            List<String> singleIPs = generateMassiveIPList(ipPattern);
            allIPs.addAll(singleIPs);
        }
        new TCPChecker().doPortScan(allIPs);
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

    public static void exit() {
        System.exit(0);
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
