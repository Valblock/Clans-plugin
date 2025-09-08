package tech.flubel.clans.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class UpdateConfig {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public UpdateConfig(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    public void fixClansConfig(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        if (!clansConfig.contains("clans")) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanupdater.no-clan"));
            return;
        }

        Set<String> clans = clansConfig.getConfigurationSection("clans").getKeys(false);

        for (String clanName : clans) {
            String basePath = "clans." + clanName + ".";

            if (!clansConfig.contains(basePath + "leader")) {
                clansConfig.set(basePath + "leader", "Unknown");
            }
            if (!clansConfig.contains(basePath + "co_leader")) {
                clansConfig.set(basePath + "co_leader", new ArrayList<>());
            }
            if (!clansConfig.contains(basePath + "members")) {
                clansConfig.set(basePath + "members", new ArrayList<>());
            }
            if (!clansConfig.contains(basePath + "prefix")) {
                clansConfig.set(basePath + "prefix", clanName);
            }
            if (!clansConfig.contains(basePath + "pvp")) {
                clansConfig.set(basePath + "pvp", true);
            }
            if (!clansConfig.contains(basePath + "joins")) {
                clansConfig.set(basePath + "joins", true);
            }
            if (!clansConfig.contains(basePath + "balance")) {
                clansConfig.set(basePath + "balance", plugin.getConfig().getInt("initial_balance", 25000));
            }
            if (!clansConfig.contains(basePath + "max_members")) {
                clansConfig.set(basePath + "max_members", plugin.getConfig().getInt("max_members", 50));
            }
            if (!clansConfig.contains(basePath + "prefix_change")) {
                clansConfig.set(basePath + "prefix_change", plugin.getConfig().getInt("prefix_change", 1));
            }
            if (!clansConfig.contains(basePath + "enemies")) {
                clansConfig.set(basePath + "enemies", new ArrayList<>());
            }
            if (!clansConfig.contains(basePath + "allies")) {
                clansConfig.set(basePath + "allies", new ArrayList<>());
            }
            if (!clansConfig.contains(basePath + "kills")) {
                clansConfig.set(basePath + "kills", 0);
            }
            if (!clansConfig.contains(basePath + "deaths")) {
                clansConfig.set(basePath + "deaths", 0);
            }
            if (!clansConfig.contains(basePath + "chest_size")) {
                clansConfig.set(basePath + "chest_size", 9);
            }
        }

        try {
            clansConfig.save(clansFile);
            player.sendMessage(ChatColor.RED + "" + ChatColor.GREEN + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GREEN + languageManager.get("clanupdater.success"));
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanupdater.error"));
            e.printStackTrace();
        }
    }
}
