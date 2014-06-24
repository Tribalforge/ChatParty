/*
 ChatParty Plugin for Minecraft Bukkit Servers
 This file: Copyright (C) 2013-2014  Dr Daniel Naylor
 Portions copyright (c) 2013-2014 Anthony Som
    
 This file is part of ChatParty.

 ChatParty is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 ChatParty is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with ChatParty.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.drnaylor.chatparty.nsfw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uk.co.drnaylor.chatparty.enums.MetadataState;
import uk.co.drnaylor.chatparty.interfaces.IChatPartyPlugin;

/**
 * Handles the NSFW channel. 
 */
public class NSFWChat {

    private final IChatPartyPlugin plugin;

    private final HashSet<String> bannedWords = new HashSet<String>();
    
    private final ArrayList<Pattern> filters = new ArrayList<Pattern>();
    
    public NSFWChat(IChatPartyPlugin plugin) {
        this.plugin = plugin;
        
        filters.add(Pattern.compile("\\b(.\\s)+(.\\b)", Pattern.CASE_INSENSITIVE));
        filters.add(Pattern.compile("\\S{2,}", Pattern.CASE_INSENSITIVE));
        filters.add(Pattern.compile("(\\b\\S\\s|)?\\S{2,}(?=(\\s\\S)\\b)?", Pattern.CASE_INSENSITIVE));
    }
    
    /**
     * Creates the set that contains the list of banned words.
     * 
     * @param strings The strings that contain the banned words.
     */
    public void setupFilter(Collection<String> strings) {
        bannedWords.clear();
        for (String s : strings) {
            bannedWords.add(s.toLowerCase());
        }
    }
    
    /**
     * Gets the set of banned words.
     * 
     * @return A set of banned words.
     * 
     * @see Set
     */
    public Set<String> getBannedWords() {
        return bannedWords;
    }
    
    /**
     * Adds a word to the ban list.
     * 
     * @param word The word to add
     * @return <code>true</code> if the word was added, <code>false</code> if the word
     *         is already in the filter.
     */
    public boolean addBannedWord(String word) {
        if (bannedWords.add(word)) {
            this.saveBannedWords();
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes a word from the ban list.
     * 
     * @param word The word to remove
     * @return <code>true</code> if the word was removed, <code>false</code> if the word
     *         was not in the filter.
     */
    public boolean removeBannedWord(String word) {
        if (bannedWords.remove(word)) {
            this.saveBannedWords();
            return true;
        }
       
        return false;
    }
    
    /**
     * Checks to see whether a banned word exists in the dictionary.
     * 
     * @param chat The chat message to scan.
     * @return <code>true</code> if a banned word is found, <code>false</code> otherwise.
     */
    public boolean containsBannedWord(String chat) {
        ChatColor.stripColor(chat);
        
        // Only get the characters we are interested in
        String chatString = chat.toLowerCase()
                .replaceAll("[^a-zA-Z01345\\s]", "")
                .replaceAll("0", "o")
                .replaceAll("1", "i")
                .replaceAll("5", "s")
                .replaceAll("4", "a")
                .replaceAll("3", "e");
        for (Pattern p : filters) {
            boolean lookahead = p.pattern().contains("(?=");
            
            Matcher matcher = p.matcher(chatString);
            while (matcher.find()) {     
                String grp = matcher.group();
                
                // Add lookahead portion.
                if (lookahead) {
                    String l = matcher.group(matcher.groupCount());
                    if (l != null) {
                        grp += l;
                    }
                }
                
                if (checkMatch(grp)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Sends a message to the NSFW chat channel
     *
     * @param sender The sender of the message, or null if sent from Console
     * @param message The message to send
     */
    public void sendNSFWMessage(Player sender, String message) {
        String tag = "*Console*";
        if (sender != null) {
            tag = sender.getDisplayName();
        }

        String formattedMessage = plugin.getNSFWChatTemplate().replace("{DISPLAYNAME}", tag).replace("{MESSAGE}", message);
        for (Player pla : getNSFWChannelPlayers()) {
            pla.sendMessage(formattedMessage);
        }
        plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
    }
    
    /**
     * Gets a list of players that are online and in the NSFW chat channel.
     * @return The list of @link{Player}s in the chat channel.
     */
    public List<Player> getNSFWChannelPlayers() {
        List<Player> players = new ArrayList<Player>();
        for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
            if (pla.hasMetadata(MetadataState.NSFWLISTENING.name())) {
                if (pla.hasPermission("chatparty.nsfw")) {
                    players.add(pla);
                } else {
                    plugin.toggleNSFWListening(pla);
                }
            }
        }
        
        return players;
    }
    
    private void saveBannedWords() {
        plugin.getConfig().set("nsfwWordFilter", new ArrayList<String>(bannedWords));
        plugin.saveConfig();
        plugin.reloadConfig();
    }
    
    private boolean checkMatch(String s) {
        String matcher = s.replaceAll("\\s", "");

        for (String w : bannedWords) {
           if (matcher.startsWith(w) || matcher.endsWith(w)) {
               return true;
           }
        }  
        
        return false;
    }
}
