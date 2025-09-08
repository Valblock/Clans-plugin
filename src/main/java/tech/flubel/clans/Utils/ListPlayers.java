package tech.flubel.clans.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListPlayers {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public ListPlayers(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }


    public void PlayerLister(Player player) {
        String clanName = getClanName(player);
        if (clanName == null || clanName.isEmpty()) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("list_players.no-clan"));
        }else{
            player.sendMessage(getClanMembers(clanName));
        }
    }

    private String getClanMembers(String clanName) {
        StringBuilder formattedMessage = new StringBuilder();
        List<String> members = new ArrayList<>();
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        // Get clan name
        String prefix = clansConfig.getString("clans." + clanName + ".prefix");
        String formattedClanName = formatClanPrefix(prefix);
        formattedMessage.append(ChatColor.GOLD + "===== " + formattedClanName + "'s" + ChatColor.GOLD + " Clan Information =====" + "\n");

        int totalPlayers = 0;

        // Add leader
        if (clansConfig.contains("clans." + clanName + ".leader")) {
            String leader = clansConfig.getString("clans." + clanName + ".leader");
            formattedMessage.append(ChatColor.GOLD + languageManager.get("list_players.leader_title") + " ")
                    .append(ChatColor.BLUE + leader + "\n");
            totalPlayers++;  // Count the leader
        }

        // Add co-leaders
            List<String> coLeaders = clansConfig.getStringList("clans." + clanName + ".co_leader");
        formattedMessage.append(ChatColor.YELLOW + languageManager.get("list_players.co-leader_title") + "\n");
            if (!coLeaders.isEmpty()) {
                formattedMessage.append(ChatColor.DARK_GREEN + String.join(", ", coLeaders) + "\n");
                totalPlayers += coLeaders.size();  // Count co-leaders
            } else {
                formattedMessage.append("\n");
            }

        // Add members
            members.addAll(clansConfig.getStringList("clans." + clanName + ".members"));
        formattedMessage.append(ChatColor.YELLOW + languageManager.get("list_players.member_title") + "\n");
            if (!members.isEmpty()) {
                formattedMessage.append(ChatColor.GREEN + String.join(", ", members) + "\n");
                totalPlayers += members.size();  // Count members
            } else {
                formattedMessage.append("\n");
            }
        formattedMessage.append(" " + "\n");

        // Show total players
        int maxmembersclan = clansConfig.getInt("clans." + clanName + ".max_members");
        formattedMessage.append(ChatColor.GOLD + languageManager.get("list_players.footer")  + " (").append(ChatColor.WHITE + String.valueOf(totalPlayers) + "/" + maxmembersclan + ChatColor.GOLD + ")\n");

        int ClanKills = clansConfig.getInt("clans." + clanName + ".kills");
        int ClanDeaths = clansConfig.getInt("clans." + clanName + ".deaths");


        double ClansKDR = (double) ClanKills / Math.max(1, ClanDeaths);

        String kdrString = String.format("%.1f", ClansKDR);

        formattedMessage.append(ChatColor.DARK_RED + languageManager.get("list_players.kdrtitle"))
                .append(" " + ChatColor.WHITE + kdrString  + "\n");
        // Add enemies
            List<String> ClanEnemies = clansConfig.getStringList("clans." + clanName + ".enemies");
        formattedMessage.append(ChatColor.BLUE  + languageManager.get("list_players.enemies") + "\n");
            if (!ClanEnemies.isEmpty()) {

                List<String> enemyPrefixes = new ArrayList<>();
                for (String enemyClan : ClanEnemies) {
                    if (clansConfig.contains("clans." + enemyClan + ".prefix")) {
                        String EnemyPrefix = clansConfig.getString("clans." + enemyClan + ".prefix");
                        String FormattedEnemyPrefix = formatClanPrefix(EnemyPrefix);
                        enemyPrefixes.add(FormattedEnemyPrefix);
                    } else {
                        enemyPrefixes.add(enemyClan);
                    }
                }

                formattedMessage.append(String.join(", ", enemyPrefixes) + "\n");
            } else {
                formattedMessage.append("\n");
            }


        // Add allies
            List<String> ClanAllies = clansConfig.getStringList("clans." + clanName + ".allies");
        formattedMessage.append(ChatColor.RED  + languageManager.get("list_players.allies") + "\n");

        if (!ClanAllies.isEmpty()) {

                List<String> allyPrefixes = new ArrayList<>();
                for (String enemyClan : ClanAllies) {
                    if (clansConfig.contains("clans." + enemyClan + ".prefix")) {
                        String AllyPrefix = clansConfig.getString("clans." + enemyClan + ".prefix");
                        String FormattedAllyPrefix = formatClanPrefix(AllyPrefix);
                        allyPrefixes.add(FormattedAllyPrefix);
                    } else {
                        allyPrefixes.add(enemyClan);
                    }
                }

                formattedMessage.append(String.join(", ", allyPrefixes) + "\n");
            } else {
                formattedMessage.append("\n");
            }

        return formattedMessage.toString();
    }


    private static String formatClanPrefix(String prefix) {
        if (prefix == null) return "";

        // Step 1: Convert hex colors (&#RRGGBB) into ChatColor.of
        // Regex finds '&#' followed by 6 hex digits
        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher matcher = hexPattern.matcher(prefix);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);

        // Step 2: Convert legacy & codes
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private String getClanName(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            if (clansConfig.getString("clans." + clanName + ".leader").equals(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".co_leader").contains(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".members").contains(player.getName())) {
                return clanName;
            }
        }
        return null;
    }

}
