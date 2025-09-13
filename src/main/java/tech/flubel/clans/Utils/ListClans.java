package tech.flubel.clans.Utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.io.File;
import java.util.*;

public class ListClans {


    public static void ClanLister(Player player, LanguageManager languageManager, Plugin plugin) {
        File clansFile = new File(player.getServer().getPluginManager().getPlugin("Clans").getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        if (!clansConfig.contains("clans")) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("list_clans.no-clans"));
            return;
        }

        Set<String> clanNamesSet = clansConfig.getConfigurationSection("clans").getKeys(false);
        if (clanNamesSet.isEmpty()) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("list_clans.no-clans"));
            return;
        }

        Map<String, Double> clanRanks = new HashMap<>();

        for (String clanName : clanNamesSet) {
            List<String> members = clansConfig.getStringList("clans." + clanName + ".members");
            List<String> coLeaders = clansConfig.getStringList("clans." + clanName + ".co_leader");
            String leader = clansConfig.getString("clans." + clanName + ".leader");

            int totalMembers = members.size() + coLeaders.size() + (leader != null ? 1 : 0);
            double balance = clansConfig.getDouble("clans." + clanName + ".balance");

            double kills = clansConfig.getDouble("clans." + clanName + ".kills");
            double deaths = clansConfig.getDouble("clans." + clanName + ".deaths");

            // Prevent division by zero
            double kdr = (deaths > 0) ? (kills / deaths) : kills;

            // Weighted rank: Members 40%, Balance 20%, KDR 40%
            double rank = (0.4 * totalMembers) + (0.2 * balance) + (0.4 * kdr);

            clanRanks.put(clanName, rank);
        }


        List<String> sortedClans = new ArrayList<>(clanNamesSet);
        sortedClans.sort((a, b) -> Double.compare(clanRanks.get(b), clanRanks.get(a)));

        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("list_clans.header"));
        for (int i = 0; i < Math.min(10, sortedClans.size()); i++) {
            String clanName = sortedClans.get(i);
            List<String> members = clansConfig.getStringList("clans." + clanName + ".members");
            List<String> coLeaders = clansConfig.getStringList("clans." + clanName + ".co_leader");

            String leader = clansConfig.getString("clans." + clanName + ".leader");
            String prefix = clansConfig.getString("clans." + clanName + ".prefix");
            String maxmembers = clansConfig.getString("clans." + clanName + ".max_members");
            String formattedClanName = formatClanPrefix(prefix);
            int totalMembers = members.size() + coLeaders.size() + (leader != null ? 1 : 0);
            double clanBalance = clansConfig.getDouble("clans." + clanName + ".balance");

            int ClanKills = clansConfig.getInt("clans." + clanName + ".kills");
            int ClanDeaths = clansConfig.getInt("clans." + clanName + ".deaths");


            double ClansKDR = (double) ClanKills / Math.max(1, ClanDeaths);

            String kdrString = String.format("%.1f", ClansKDR);

            player.sendMessage(ChatColor.BOLD + "" + (i + 1) + ") " + ChatColor.YELLOW + formattedClanName + ChatColor.YELLOW + " | " + languageManager.get("list_clans.leader_title") + ": " + leader + " ("
                    + totalMembers + "/" + maxmembers + ") | $" + clanBalance + " | " + kdrString + " KDR");
        }
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
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);

        // Step 2: Convert legacy & codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }


}
