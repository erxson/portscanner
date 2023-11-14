package net.sourceforge.queried;

/**
 * Server information that is returned by the server query.
 * 
 * @author DeadEd
 */
public class ServerInfo {

    private String name = "";
    private String ip = "";
    private String port = "";
    private String game = "";
    private String gameVersion = "";
    private String map = "";
    private String playerCount = "";
    private String maxPlayers = "";
    private String team1Tickets = "";
    private String team2Tickets = "";

    private String fullResponse = "";

    /**
     * Get the game type.
     * 
     * @return the game type.
     */
    public String getGame() {
        return game;
    }

    /**
     * Get the game version.
     * 
     * @return the game version.
     */
    public String getGameVersion() {
        return gameVersion;
    }

    /**
     * Get the server IP.
     * 
     * @return the server IP.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Get the map currently being played on the server.
     * 
     * @return the map.
     */
    public String getMap() {
        return map;
    }

    /**
     * Get the maximum number of players allowed on the server.
     * 
     * @return max players.
     */
    public String getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Get the name of the server.
     * 
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get how many players are currently on the server.
     * 
     * @return the player count.
     */
    public String getPlayerCount() {
        return playerCount;
    }

    /**
     * Get the port the server is using.
     * 
     * @return the port.
     */
    public String getPort() {
        return port;
    }
    
    public void setGame(String string) {
        game = string;
    }


    public void setIp(String string) {
        ip = string;
    }

    public void setMap(String string) {
        map = string;
    }

    public void setMaxPlayers(String string) {
        maxPlayers = string;
    }

    public void setName(String string) {
        name = string;
    }

    public void setPlayerCount(String string) {
        playerCount = string;
    }

    public void setPort(String string) {
        port = string;
    }

    public String getTeam1Tickets() {
        return team1Tickets;
    }

    public String getTeam2Tickets() {
        return team2Tickets;
    }

	public String getFullResponse() {
		return fullResponse;
	}

	public void setFullResponse(String fullResponse) {
		this.fullResponse = fullResponse;
	}

}
