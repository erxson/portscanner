package net.sourceforge.queried;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author DeadEd
 */
public class Util {

    public static DatagramPacket getDatagramPacket(String request,
            InetAddress inet, int port) {
        byte first = -1;
        byte[] buffer = new byte[1400];
        buffer[0] = first;
        buffer[1] = first;
        buffer[2] = first;
        buffer[3] = first;
        byte[] requestBytes;
        requestBytes = request.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(requestBytes, 0, buffer, 4, request.length());

        return new DatagramPacket(buffer, request.length() + 4, inet, port);
    }

    public static String getInfo(int localPort, String ipStr, int port,
            String request, int infoType, int queryType) {

        StringBuffer recStr;
        DatagramSocket socket = null;
        try {
            if (localPort == 0) {
                socket = new DatagramSocket();
            } else {
                socket = new DatagramSocket(localPort);
            }
            // default packet size
            int packetSize = 12288;

            InetAddress address = InetAddress.getByName(ipStr);
            InetAddress inet = InetAddress.getByName(ipStr);

            socket.connect(address, port);

            DatagramPacket out = getDatagramPacket(request, inet, port);
            socket.send(out);

            byte[] data = new byte[packetSize];
            DatagramPacket inPacket = new DatagramPacket(data, packetSize);
            socket.setSoTimeout(QueriEd.TIMEOUT);

            // get the response
            socket.receive(inPacket);

            recStr = new StringBuffer(new String(inPacket.getData(), 0,
                    inPacket.getLength(), StandardCharsets.ISO_8859_1));
            if ((queryType == QueriEd.QUERY_SOURCE || queryType == QueriEd.QUERY_HALFLIFE)
                    && infoType == QueriEd.INFO_PLAYERS) {
                // we just got a challenge, need to go back again with the
                // request + challenge
                String sourceChallenge = recStr
                        .substring(recStr.indexOf("A") + 1);
                out = getDatagramPacket("U" + sourceChallenge, inet, port);
                socket.send(out);
                byte[] data2 = new byte[packetSize];

                DatagramPacket inPacket2 = new DatagramPacket(data2, packetSize);
                // get the response
                socket.receive(inPacket2);
                recStr = new StringBuffer(new String(inPacket2.getData(), 0,
                        inPacket2.getLength(), StandardCharsets.ISO_8859_1));
            }

        } catch (Exception ex) {
            recStr = new StringBuffer();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return recStr.toString();
    }
}