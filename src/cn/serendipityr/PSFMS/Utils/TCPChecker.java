package cn.serendipityr.PSFMS.Utils;

import com.alibaba.fastjson.JSONObject;
import net.sourceforge.queried.PlayerInfo;
import net.sourceforge.queried.QueriEd;
import net.sourceforge.queried.ServerInfo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TCPChecker {

    private static String removeAnsiCodes(String text) {
        String ansiPattern = "\u001B\\[[0-9;]*m";
        Pattern pattern = Pattern.compile(ansiPattern);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }

    public static Boolean checkPortOpen(InetSocketAddress inetSocketAddress) {
        boolean isOpen;
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress, ConfigUtil.ConnectTimeout);
            socket.setSoTimeout(ConfigUtil.ReadTimeout);
            isOpen = true;
        } catch (Exception e) {
            if (ConfigUtil.ShowFails) {
                String msg = "Fails | " + inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort() + " | " + e;
                //LogUtil.saveOpeningPort(msg);
                LogUtil.doLog(-1, msg + "\n", "PortScan");
            }
            isOpen = false;
        }

        return isOpen;
    }

    public static CompletableFuture<Boolean> checkPortOpenAsync(InetSocketAddress inetSocketAddress, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> checkPortOpen(inetSocketAddress), executor);
    }

    private static void check(String gameType, String ip, int port) throws UnsupportedEncodingException {

        ServerInfo serverInfo = QueriEd.serverQuery(gameType, ip, port);

        if (serverInfo != null) {
            System.out.println(
                    "VALVE | " + ip + ":" + port + " " +
                            serverInfo.getName() +
                            "\nGame: " + serverInfo.getGame() +
                            "\nMap: " + serverInfo.getMap() +
                            "\nСписок игроков: (" + serverInfo.getPlayerCount() + " / " + serverInfo.getMaxPlayers() + "):");
        }

        ArrayList playerInfo = QueriEd.playerQuery(gameType, ip, port);

        if (playerInfo != null && playerInfo.size() > 0) {
            Iterator iter = playerInfo.iterator();
            int count = 1;

            while (iter.hasNext()) {
                PlayerInfo pInfo = (PlayerInfo) iter.next();

                System.out.println(
                        count + ") " + pInfo.getName() + " [" + pInfo.getScore()
                                + "/" + pInfo.getKills() + "/" + pInfo.getDeaths() + "]");
                count++;
            }
        }
    }

    public void doPortScan(List<String> addresses) {
        ExecutorService addressExecutor = Executors.newFixedThreadPool(ConfigUtil.AddressThreads);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String address : addresses) {
            CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
                ExecutorService portExecutor = Executors.newFixedThreadPool(ConfigUtil.ScanThreads);
                if (ConfigUtil.LogCurrentIP) LogUtil.doLog(-1, "Scanning " + address + "\n", "PortScan");

                List<CompletableFuture<Void>> portFutures = new ArrayList<>();

                for (int i = ConfigUtil.MinPort; i <= ConfigUtil.MaxPort; i++) {
                    int port = i;
                    CompletableFuture<Boolean> portFuture = checkPortOpenAsync(new InetSocketAddress(address, port), portExecutor);
                    portFutures.add(portFuture.thenAccept(isOpen -> {
                        if (ConfigUtil.LogKonterStriker) {
                            try {
                                check("HL", address, port);
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (isOpen) {
                            JSONObject minecraftServerInfo = null;
                            boolean isHttpWebsite = false;

                            try {
                                if (ConfigUtil.LogMinecraft) {
                                    TimeUnit.MILLISECONDS.sleep(ConfigUtil.ScanDelay);
                                    minecraftServerInfo = MCChecker.checkMinecraftServer(new InetSocketAddress(address, port));
                                }
                                if (ConfigUtil.LogHTTP) {
                                    TimeUnit.MILLISECONDS.sleep(ConfigUtil.ScanDelay);
                                    isHttpWebsite = isHTTPWebsite(new InetSocketAddress(address, port));
                                }
                            } catch (InterruptedException ignored) {
                            }

                            String logMessage = "";
                            if (minecraftServerInfo != null && ConfigUtil.LogMinecraft) {
                                logMessage = "MC | " + address + ":" + port + " | " + MCChecker.getMinecraftServerInfo(minecraftServerInfo);
                            } else if (isHttpWebsite && ConfigUtil.LogHTTP) {
                                String body = getHTTPTitleAndContent(new InetSocketAddress(address, port));
                                logMessage = "HTTP | http" + (port == 443 || port == 8443 ? "s" : "") + "://" + address + ":" + port + body;
                            } else if (ConfigUtil.LogTCP) {
                                logMessage = "TCP | " + address + ":" + port;
                            }

                            if (!logMessage.isEmpty()) {
                                LogUtil.saveOpeningPort(removeAnsiCodes(logMessage));
                                LogUtil.doLog(-1, logMessage + "\n", "PortScan");
                            }
                        }
                    }));
                }

                CompletableFuture<Void> allPortFutures = CompletableFuture.allOf(portFutures.toArray(new CompletableFuture[0]));
                try {
                    allPortFutures.get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
                portExecutor.shutdown();
            }, addressExecutor);

            futures.add(addressFuture);
        }

        CompletableFuture<Void> allAddressFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allAddressFutures.get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        addressExecutor.shutdown();
    }

    private boolean isHTTPWebsite(InetSocketAddress address) {
        try (Socket socket = new Socket()) {
            socket.connect(address, ConfigUtil.ConnectTimeout);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("HEAD / HTTP/1.1");
            out.println("Host: " + address.getHostName());
            out.println();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = in.readLine();
            if (line != null) {
                LogUtil.println("Body: " + line);
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private String getHTTPTitleAndContent(InetSocketAddress address) {
        try (Socket socket = new Socket()) {
            socket.connect(address, ConfigUtil.ConnectTimeout);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("GET / HTTP/1.1");
            out.println("Host: " + address.getHostName());
            out.println();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String headerLine;
            String redirectUrl = null;
            while ((headerLine = in.readLine()) != null && headerLine.length() > 0) {
                if (headerLine.startsWith("Content-Type: text/html")) {
                    break;
                } else if (headerLine.startsWith("Location: ")) {
                    redirectUrl = headerLine.substring(10);
                }
            }

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("<title>")) {
                    String title = line.substring(7, line.length() - 8);
                    if (!ConfigUtil.TitleSearch.isEmpty()) {
                        if (ConfigUtil.TitleSearch.contains(title)) return " - " + title;
                        return "";
                    }
                    if (title.equals("Page not found") || ConfigUtil.TitleExclude.contains(title)) return "";
                    return " - " + title;
                }
            }

            if (redirectUrl != null) {
                return " - (" + redirectUrl + ")";
            }

            return "";
        } catch (IOException ignored) {
            return "";
        }
    }
}
