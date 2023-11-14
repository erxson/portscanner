package net.sourceforge.queried;

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.queried.gametypes.HLServerInfo;
import net.sourceforge.queried.gametypes.SourceServerInfo;
/**
 * @author DeadEd
 */
public class QueriEd {

    /**
     * Timeout used for the sockets 
     */
    public static final int TIMEOUT = 2000;

    /**
     * Server details 
     */
    public static final int INFO_DETAILS = 0;
    /**
     *  Player details 
     */
    public static final int INFO_PLAYERS = 1;

    /**
     * Halflife query type 
     */
    public static final int QUERY_HALFLIFE = 1;
    /**
     * Halflife 2 / Source query type 
     */
    public static final int QUERY_SOURCE = 4;
    /**
     * Halflife game type 
     */
    public static final int GAME_HL = 5;
    /**
     * Halflife 2 / Source game type 
     */
    public static final int GAME_HL2 = 11;
    
    
    private static final HashMap supportedGames = new HashMap();
    static {
        supportedGames.put("HL", "Halflife");
        supportedGames.put("HL2", "Halflife 2");
    }

    /**
     * Returns a HashMap of the games that QueriEd supports.
     * The key is the game code.  The full game name is the value.
     * 
     * @return a HashMap of the support games
     */
    public static HashMap getSupportedGames() {
        return supportedGames;
    }
    
    /**
     * &ServerInfo serverInfo = QueriEd.serverQuery(27777, "HL", ip, port);</code>
     * 
     * @param localPort a port on the machine that the bot is running from that will be used to make the query
     * @param gameType one of the supported game types, defaults to Halflife
     * @param ipStr the ip (numerical or hostname) of the server
     * @param port the query port of the server
     * @return a ServerInfo object, or null if there was some problem wheil querying the server
     */
    public static ServerInfo serverQuery(int localPort, String gameType, String ipStr, int port) {
        int resolvedGameType = resolve(gameType);

        return serverQuery(localPort, resolvedGameType, ipStr, port, INFO_DETAILS);
    }
    
    /**
     * ServerInfo serverInfo = QueriEd.serverQuery("HL", ip, port);</code>
     * 
     * @param gameType one of the supported game types, defaults to Halflife
     * @param ipStr the ip (numerical or hostname) of the server
     * @param port the query port of the server
     * @return a ServerInfo object, or null if there was some problem wheil querying the server
     */
    public static ServerInfo serverQuery(String gameType, String ipStr, int port) {
        return serverQuery(0, gameType, ipStr, port);
    }
    
    /**
     * ArrayList playerInfo = QueriEd.playerQuery(27777, "HL", ip, port);</code>
     * 
     * @param localPort a port on the machine that the bot is running from that will be used to make the query
     * @param gameType one of the supported game types, defaults to Halflife
     * @param ipStr the ip (numerical or hostname) of the server
     * @param port the query port of the server
     * @return an ArrayList of PlayerInfo objects, the list will be empty if there aren't any players 
     * on the server
     */
    public static ArrayList playerQuery(int localPort, String gameType, String ipStr, int port) {
        int resolvedGameType = resolve(gameType);

        return playerQuery(localPort, resolvedGameType, ipStr, port, INFO_PLAYERS);
    }

    /**
     * ArrayList playerInfo = QueriEd.playerQuery("HL", ip, port);</code>
     * 
     * @param gameType one of the supported game types, defaults to Halflife
     * @param ipStr the ip (numerical or hostname) of the server
     * @param port the query port of the server
     * @return an ArrayList of PlayerInfo objects, the list will be empty if there aren't any players 
     * on the server
     */
    public static ArrayList playerQuery(String gameType, String ipStr, int port) {
        return playerQuery(0, gameType, ipStr, port);
    }
    
    private static int resolve(String gameType) {
        if(gameType.equalsIgnoreCase("HL")) {
            return GAME_HL;
        } else if(gameType.equalsIgnoreCase("HL2")) {
            return GAME_HL2;
        } else {
            return GAME_HL;
        }
    }
    
    private static ServerInfo serverQuery(int localPort, int gameType, String ipStr, int port, int infoType) {
        
        switch (gameType) {
            case GAME_HL:
                return HLServerInfo.getDetails(localPort, ipStr, port, infoType, QUERY_HALFLIFE);
            case GAME_HL2:
                return SourceServerInfo.getDetails(localPort, ipStr, port, infoType, QUERY_SOURCE);
            default:
                return HLServerInfo.getDetails(localPort, ipStr, port, infoType, QUERY_HALFLIFE);
        }

    }

    private static ArrayList playerQuery(int localPort, int gameType, String ipStr, int port, int infoType) {
        
        switch (gameType) {
            case GAME_HL :
                return HLServerInfo.getPlayers(localPort, ipStr, port, infoType, QUERY_HALFLIFE);
            case GAME_HL2 :
                return SourceServerInfo.getPlayers(localPort, ipStr, port, infoType, QUERY_SOURCE);
            default :
                return HLServerInfo.getPlayers(localPort, ipStr, port, infoType, QUERY_HALFLIFE);
        }

    }

}
