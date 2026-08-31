package tech.flubel.clans.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class SearchPlayer {

    private final JavaPlugin plugin;

    public SearchPlayer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPlayerInClan(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        // co_leader est une LISTE. getString rendait donc null, et le
        // .equals qui suivait levait une NullPointerException des qu'un seul
        // clan existait - le bug ne se voyait pas tant que clans.yml etait
        // vide, puisque la boucle ne s'executait jamais.
        if (clansConfig.getConfigurationSection("clans") == null) return false;

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            List<String> members = clansConfig.getStringList("clans." + clanName + ".members");
            List<String> coLeaders = clansConfig.getStringList("clans." + clanName + ".co_leader");
            String leader = clansConfig.getString("clans." + clanName + ".leader");

            if (members.contains(player.getName())
                    || coLeaders.contains(player.getName())
                    || player.getName().equals(leader)) {
                return true;
            }
        }
        return false;
    }
}
