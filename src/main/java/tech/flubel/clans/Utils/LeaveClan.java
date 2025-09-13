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
import java.util.List;

public class LeaveClan {
    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public LeaveClan(JavaPlugin plugin, LanguageManager languageManager){
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    public void clanleaver(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);
        String ClanName = getClanName(player);

        if (!config.contains("clans")) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("leave.no-clan-err"));
            return;
        }
        if(ClanName == null){
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("leave.no-clan"));
        }

        String leader = config.getString("clans." + ClanName + ".leader");

        if (player.getName().equals(leader)) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("leave.leader-warn"));
            return;
        }

            List<String> members = config.getStringList("clans." + ClanName + ".members");
            List<String> coleaders = config.getStringList("clans." + ClanName + ".co_leader");

                if (members.contains(player.getName())) {
                    members.remove(player.getName());
                    config.set("clans." + ClanName + ".members", members);
                }

                // Remove from co-leaders
                if (coleaders.contains(player.getName())) {
                    coleaders.remove(player.getName());
                    config.set("clans." + ClanName + ".co_leader", coleaders);
                }


            int playerKills = player.getStatistic(Statistic.PLAYER_KILLS);
            int playerDeaths = player.getStatistic(Statistic.DEATHS);

            int clanKills = config.getInt("clans." + ClanName + ".kills", 0);
            int clanDeaths = config.getInt("clans." + ClanName + ".deaths", 0);

            clanKills -= playerKills;
            clanDeaths -= playerDeaths;

            config.set("clans." + ClanName + ".kills", clanKills);
            config.set("clans." + ClanName + ".deaths", clanDeaths);


                try {
                    config.save(clansFile);
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("leave.success"));
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("leave.error"));
                    e.printStackTrace();
                }

//        player.sendMessage(ChatColor.RED + "You are not in a clan.");


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