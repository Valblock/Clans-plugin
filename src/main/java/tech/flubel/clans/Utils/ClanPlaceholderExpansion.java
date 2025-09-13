package tech.flubel.clans.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tech.flubel.clans.Clans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanPlaceholderExpansion extends PlaceholderExpansion {

    private final Clans plugin;

    public ClanPlaceholderExpansion(Clans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "clans";
    }

    @Override
    public String getAuthor() {
        return "Flubel";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        if (identifier.equals("name")) {
            return getPlayerClan(player);
        }
        if (identifier.equals("name_cm")) {
            return getPlayerClanChatManager(player);
        }
        if (identifier.equals("name_plain")) {
            return getPlainClanName(player);
        }
        if (identifier.equals("badge")) {
            return getPlayerClanBadge(player);
        }
        if (identifier.startsWith("list_")) {
            return getTopClanInfo(identifier);
        }
        return null;
    }

    private String getPlayerClan(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            if (clansConfig.getString("clans." + clanName + ".leader").equals(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".co_leader").contains(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".members").contains(player.getName())) {
                return formatClanPrefix(clansConfig.getString("clans." + clanName + ".prefix") + " ");
            }
        }
        return "";
    }

    private String getPlayerClanChatManager(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            if (clansConfig.getString("clans." + clanName + ".leader").equals(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".co_leader").contains(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".members").contains(player.getName())) {

                String prefix = clansConfig.getString("clans." + clanName + ".prefix");

                if (prefix != null) {
                    prefix = prefix.replaceAll("&(?=#)", "");

                    if (!prefix.isEmpty()) {
                        return prefix + " ";
                    }
                }

                return "";
            }
        }
        return "";
    }


    private String getPlainClanName(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            if (clansConfig.getString("clans." + clanName + ".leader").equals(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".co_leader").contains(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".members").contains(player.getName())) {

                String prefix = clansConfig.getString("clans." + clanName + ".prefix");
                String cleanedClanName = prefix.replaceAll("&[a-zA-Z0-9]", "").replaceAll("&#[a-fA-F0-9]{6}", "");

                return cleanedClanName;
            }
        }
        return "";
    }


    private String getTopClanInfo(String identifier) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        if (!clansConfig.contains("clans")) return "";

        // Calculate ranks
        Map<String, Double> clanRanks = new HashMap<>();
        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            int members = clansConfig.getStringList("clans." + clanName + ".members").size();
            int coLeaders = clansConfig.getStringList("clans." + clanName + ".co_leader").size();
            String leader = clansConfig.getString("clans." + clanName + ".leader");

            int totalMembers = members + coLeaders + (leader != null ? 1 : 0);
            double balance = clansConfig.getDouble("clans." + clanName + ".balance");

            double rank = (0.6 * totalMembers) + (0.4 * balance);
            clanRanks.put(clanName, rank);
        }

        List<String> sortedClans = new ArrayList<>(clanRanks.keySet());
        sortedClans.sort((a, b) -> Double.compare(clanRanks.get(b), clanRanks.get(a)));

        // Parse identifier (like list_1_name)
        try {
            String[] parts = identifier.split("_"); // [list, 1, name]
            int index = Integer.parseInt(parts[1]) - 1;
            String type = parts[2];

            if (index < 0 || index >= sortedClans.size()) return "";

            String clanName = sortedClans.get(index);

            switch (type.toLowerCase()) {
                case "name":
                    return (formatClanPrefix(clansConfig.getString("clans." + clanName + ".prefix", clanName)));
                case "leader":
                    return clansConfig.getString("clans." + clanName + ".leader", "---");
                case "balance":
                    return String.valueOf(clansConfig.getDouble("clans." + clanName + ".balance", 0.0));
                case "kdr":
                    int clanKills = clansConfig.getInt("clans." + clanName + ".kills", 0);
                    int clanDeaths = clansConfig.getInt("clans." + clanName + ".deaths", 0);

                    if (clanDeaths == 0) {
                        return String.valueOf(clanKills);
                    }

                    double kdr = (double) clanKills / clanDeaths;
                    return String.format("%.1f", kdr);
                default:
                    return "---";
            }
        } catch (Exception e) {
            return "---";
        }
    }

    private String getPlayerClanBadge(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            if (clansConfig.getString("clans." + clanName + ".leader").equals(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".co_leader").contains(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".members").contains(player.getName())) {

                String prefix = clansConfig.getString("clans." + clanName + ".prefix");

                if (prefix != null && !prefix.isEmpty()) {
                    String colorCodes = "";

                    // Handle hex color if present
                    if (prefix.startsWith("&#") && prefix.length() >= 8) {
                        String hex = "#" + prefix.substring(2, 8);
                        try {
                            colorCodes += ChatColor.of(hex);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid hex in clan prefix: " + prefix);
                        }
                    }

                    // Handle legacy & codes
                    for (int i = 0; i < prefix.length(); i++) {
                        if (prefix.charAt(i) == '&' && i + 1 < prefix.length()) {
                            char code = prefix.charAt(i + 1);
                            if (ChatColor.getByChar(code) != null) {
                                colorCodes += ChatColor.getByChar(code);
                            }
                        } else if (Character.isLetter(prefix.charAt(i)) || Character.isDigit(prefix.charAt(i))) {
                            break;
                        }
                    }

                    prefix = colorCodes + "🛡";
                } else {
                    prefix = ChatColor.GOLD + "🛡";
                }


                return prefix;
            }
        }

        return "";
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



}
