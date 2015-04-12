/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.drnaylor.chatparty.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Daniel
 */
public final class Utilities {

    private Utilities() {
    }
    
    /**
     * Sorts a list. See http://stackoverflow.com/a/740351/3032166.
     * @param <T> Type of collection to sort.
     * @param c The collection.
     * @return A sorted list.
     */
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }
    
    /**
     * Checks to see if the specified player is contained in the specified list.
     * @param players The list of {@link OfflinePlayer} objects.
     * @param player The player to check for.
     * @return <code>true</code> if the player is included in the list.
     */
    public static boolean listContainsPlayer(Collection<OfflinePlayer> players, OfflinePlayer player) {
        // First, do a hash check
        if (players.contains(player)) {
            return true;
        }
        
        // Else, slower UUID check
        for (OfflinePlayer op : players) {
            if (player.getUniqueId().equals(op.getUniqueId())) {
                return true;
            }
        }
        
        return false;
    }
}
