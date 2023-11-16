package cn.serendipityr.PSFMS.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static cn.serendipityr.PSFMS.Utils.ConfigUtil.LogPlayerList;
import static cn.serendipityr.PSFMS.Utils.ConfigUtil.LogVersion;

public class MCChecker {

    public static JSONObject checkMinecraftServer(InetSocketAddress inetSocketAddress) {
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress, ConfigUtil.ConnectTimeout);
            socket.setSoTimeout(ConfigUtil.ReadTimeout);

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(b);

            handshake.writeByte(0x00);
            if (ConfigUtil.VersionProtocol != 0) writeVarInt(handshake, ConfigUtil.VersionProtocol);
            writeVarInt(handshake, 4);
            writeVarInt(handshake, ConfigUtil.ScanAddress.length());
            handshake.writeBytes(ConfigUtil.ScanAddress);
            handshake.writeShort(inetSocketAddress.getPort());
            writeVarInt(handshake, 1);

            writeVarInt(dataOutputStream, b.size());
            dataOutputStream.write(b.toByteArray());
            dataOutputStream.writeByte(0x01);
            dataOutputStream.writeByte(0x00);

            String result = readFromInputStream(dataInputStream);
            dataOutputStream.flush();
            socket.close();
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMinecraftServerInfo(JSONObject minecraftServerInfo) {
        StringBuilder info = new StringBuilder();

        StringBuilder motd = new StringBuilder();
        if (minecraftServerInfo.get("description") instanceof JSONObject) {
            JSONObject motdArray = minecraftServerInfo.getJSONObject("description");
            JSONArray extraArray = motdArray.getJSONArray("extraArray");

            if (extraArray != null) {
                for (int i = 0; i < extraArray.size(); i++) {
                    JSONObject obj = extraArray.getJSONObject(i);
                    String color = obj.getString("color");
                    String text = obj.getString("text");
                    motd.append(getAnsiColorCode(color)).append(text).append("\u001B[0m");
                }
            } else if (motdArray.containsKey("text")) {
                String text = motdArray.getString("text");
                motd.append(text);
            } else {
                motd.append("no motd(");
            }
        } else if (minecraftServerInfo.getString("description") != null) {
            motd = new StringBuilder(minecraftServerInfo.getString("description"));
        }

        info.append(" ").append(motd).append("\n");
        Integer onlinePlayers = minecraftServerInfo.getJSONObject("players").getInteger("online");
        Integer maxPlayers = minecraftServerInfo.getJSONObject("players").getInteger("max");

        if (LogPlayerList) {
            JSONObject players = minecraftServerInfo.getJSONObject("players");
            if (players.containsKey("sample")) {
                JSONArray playersArray = players.getJSONArray("sample");
                info.append("    Список игроков (").append(onlinePlayers).append(" / ").append(maxPlayers).append("):\n");
                for (int i = 0; i < playersArray.size(); i++) {
                    JSONObject playerObj = playersArray.getJSONObject(i);
                    String playerName = playerObj.getString("name");
                    String playerId = playerObj.getString("id");
                    info.append("      - ").append(playerName).append(" (ID: ").append(playerId).append(")\n");
                }
            }/* else if (players.containsKey("online") || players.containsKey("max")) {
                info.append("Debug: ");
                info.append(minecraftServerInfo.getJSONObject("players").toJSONString());
            }*/
            else {
                System.out.println(players.toJSONString());
            }
        }

        String version = minecraftServerInfo.getJSONObject("version").getString("name");
        Integer protocolVersion = minecraftServerInfo.getJSONObject("version").getInteger("protocol");
        if (LogVersion) info.append("    Версия: ").append(version).append(" (").append(protocolVersion).append(")");

        return info.toString();
    }

    private static String getAnsiColorCode(String color) {
        return switch (color) {
            case "black" -> "\u001B[30m";
            case "dark_blue" -> "\u001B[34m";
            case "dark_green" -> "\u001B[32m";
            case "dark_aqua" -> "\u001B[36m";
            case "dark_red" -> "\u001B[31m";
            case "dark_purple" -> "\u001B[35m";
            case "gold" -> "\u001B[33m";
            case "gray" -> "\u001B[37m";
            case "dark_gray" -> "\u001B[90m";
            case "blue" -> "\u001B[94m";
            case "green" -> "\u001B[92m";
            case "aqua" -> "\u001B[96m";
            case "red" -> "\u001B[91m";
            case "light_purple" -> "\u001B[95m";
            case "yellow" -> "\u001B[93m";
            case "white" -> "\u001B[97m";
            default -> "";
        };
    }

    public static String readFromInputStream(DataInputStream dataInputStream) throws IOException {
        readVarInt(dataInputStream);
        readVarInt(dataInputStream);

        int len = readVarInt(dataInputStream);
        byte[] bytes = new byte[len];
        dataInputStream.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int a = 0;
        int b = 0;
        while (true) {
            int c = in.readByte();

            a |= (c & 0x7F) << b++ * 7;

            if (b > 5)
                throw new RuntimeException("VarInt too big");

            if ((c & 0x80) != 128)
                break;
        }
        return a;
    }

    public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }
}
