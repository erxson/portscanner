package net.sourceforge.queried.gametypes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import net.sourceforge.queried.KillsComparator;
import net.sourceforge.queried.PlayerInfo;
import net.sourceforge.queried.ServerInfo;
import net.sourceforge.queried.Util;


public class HLServerInfo {

    public static ArrayList getPlayers(int localPort, String ipStr, int port, int infoType, int queryType) {
        /** On some operating systems the following code will return the wrong number of bytes 
         * in Unicode to UTF-8 conversion
         * 
         * a) byte.getBytes("UTF-8")
         * 
         * b) byte.getBytes() if LANG=en_US.UTF-8 is set in the OS enviroment
         * 
         * The two possible workarounds are:
         * 
         * 'unset LANG' in the OS shell before running your Java code
         * 
         * or 
         * 
         * use byte.getBytes("ISO-8859-1") or any other encoding than UTF-8
         * 
         */
        byte[] buf = null;
        buf = Util.getInfo(localPort, ipStr, port, "UA2S_PLAYER", infoType, queryType).getBytes(StandardCharsets.ISO_8859_1);
        if(buf.length == 0) {
            return null;            
        } 
        else if(buf[0] != buf[1] || buf[1] != buf[2] || buf[2] != buf[3] || buf[4] != 'D') {
            return null;            
        }

        ArrayList sorted = new ArrayList();
        int playerCount = buf[5] & 255;
        int off = 6;
        for(int i=0; i<playerCount; ++i) {
            PlayerInfo playerInfo = new PlayerInfo();
            StringBuffer playerName = new StringBuffer(20);
            while(buf[off] != 0) {
                playerName.append((char)(buf[off++] & 255));
            }
            off++;
            playerInfo.setName(playerName.toString().trim());
            playerInfo.setKills((buf[off] & 255) | ((buf[off+1] & 255) << 8) | 
                ((buf[off+2] & 255) << 16) | ((buf[off+3] & 255) << 24));
            sorted.add(playerInfo);
            off += 8;
        }
        Collections.sort(sorted, new KillsComparator());
        
        return sorted;
    }

    public static ServerInfo getDetails(int localPort, String ipStr, int port, int infoType, int queryType) {
        /** On some operating systems the following code will return the wrong number of bytes 
         * in Unicode to UTF-8 conversion
         * 
         * a) byte.getBytes("UTF-8")
         * 
         * b) byte.getBytes() if LANG=en_US.UTF-8 is set in the OS enviroment
         * 
         * The two possible workarounds are:
         * 
         * 'unset LANG' in the OS shell before running your Java code
         * 
         * or 
         * 
         * use byte.getBytes("ISO-8859-1") or any other encoding than UTF-8
         * 
         */
        byte[] buf = null;
        buf = Util.getInfo(localPort, ipStr, port, "TSource Engine Query", infoType, queryType).getBytes(StandardCharsets.ISO_8859_1);


        if(buf.length == 0) {
            return null;            
        } else if(buf[0] != buf[1] || buf[1] != buf[2] || buf[2] != buf[3] || buf[4] != 'm') { 
            return null;            
        } 
        
        ServerInfo serverInfo = new ServerInfo();
        
        int off = 5;

        InetAddress inettst;
        try {
            inettst = InetAddress.getByName(ipStr);
            serverInfo.setIp(inettst.getHostAddress());
        } catch (UnknownHostException e) {
            serverInfo.setIp(ipStr);
        }

        serverInfo.setPort(String.valueOf(port));

        while(buf[off] != 0) {
            off++;
        }

        off++;

        StringBuffer netName = new StringBuffer(20);
        while(buf[off] != 0) {
            netName.append((char)(buf[off++] & 255));
        }
        serverInfo.setName(netName.toString());
        
        off++;
        
        StringBuffer mapName = new StringBuffer(20);
        while(buf[off] != 0) {
            mapName.append((char)(buf[off++] & 255));
        }
        serverInfo.setMap(mapName.toString());
        
        off++;

        // skip game directory
        while(buf[off] != 0) {
            off++;
        }
        
        off++;

        StringBuffer gameDesc = new StringBuffer(20);
        while(buf[off] != 0) {
            gameDesc.append((char)(buf[off++] & 255));
        }
        serverInfo.setGame(gameDesc.toString());

        off++;
        
        int playerCount = buf[off] & 255;
        serverInfo.setPlayerCount(String.valueOf(playerCount));
        
        off++;
        
        int maxPlayerCount = buf[off] & 255;
        serverInfo.setMaxPlayers(String.valueOf(maxPlayerCount));
        serverInfo.setFullResponse(new String(buf));

        return serverInfo;
    }
    
}
