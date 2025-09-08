package tech.flubel.clans.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPlayer {
    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public AddPlayer(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }


    public void PlayerAdder(String clanName, Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        ArrayList<String> membersList = (ArrayList<String>) clansConfig.getStringList("clans." + clanName + ".members");

        MemberCount memberCount = new MemberCount(plugin);
        int currentMembers = memberCount.getClanMembersCount(clanName);

        if(currentMembers >= clansConfig.getInt("clans." + clanName + ".max_members")){
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("add.full"));
            return;
        }


        if (!membersList.contains(player.getName())) {
            membersList.add(player.getName());
            clansConfig.set("clans." + clanName + ".members", membersList);


            int playerKills = player.getStatistic(Statistic.PLAYER_KILLS);
            int playerDeaths = player.getStatistic(Statistic.DEATHS);

            int clanKills = clansConfig.getInt("clans." + clanName + ".kills", 0);
            int clanDeaths = clansConfig.getInt("clans." + clanName + ".deaths", 0);

            clanKills += playerKills;
            clanDeaths += playerDeaths;

            clansConfig.set("clans." + clanName + ".kills", clanKills);
            clansConfig.set("clans." + clanName + ".deaths", clanDeaths);

            try {
                clansConfig.save(clansFile);

                String prefix = clansConfig.getString("clans." + clanName + ".prefix");
                String TranslatedClanName = formatClanPrefix(prefix);


                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("clan_name", TranslatedClanName);

                String message = languageManager.get("add.success", placeholders);
                message = message.replace(TranslatedClanName, TranslatedClanName + ChatColor.GREEN);

                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GREEN + message);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("add.error"));
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.YELLOW + languageManager.get("add.alr-mem"));
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
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);

        // Step 2: Convert legacy & codes
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

}
