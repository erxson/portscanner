package net.sourceforge.queried;

import java.util.Comparator;

public class KillsComparator implements Comparator {

    public KillsComparator() {
    }

    public int compare(Object obj1, Object obj2) {
        PlayerInfo playerInfo1 = (PlayerInfo) obj1;
        PlayerInfo playerInfo2 = (PlayerInfo) obj2;

        if(playerInfo1.getKills() < playerInfo2.getKills()) {
            return 1;
        }

        return playerInfo1.getKills() <= playerInfo2.getKills() ? 0 : -1;
    }
}
