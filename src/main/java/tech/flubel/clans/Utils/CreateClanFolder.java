package tech.flubel.clans.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CreateClanFolder {

    private final JavaPlugin plugin;

    public CreateClanFolder(JavaPlugin plugin) {
        this.plugin = plugin;
        createFolders();
    }

    private void createFolders() {
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File clansFile = new File(pluginFolder, "clans.yml");
        if (!clansFile.exists()) {
            try {
                clansFile.createNewFile();

                FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);
                clansConfig.createSection("clans");
                clansConfig.save(clansFile);


            } catch (IOException e) {
                plugin.getLogger().severe("Could not create clans.yml file: " + e.getMessage());
            }
        }


        File clansChestFile = new File(pluginFolder, "clans_chest.yml");
        if (!clansChestFile.exists()) {
            try {
                clansChestFile.createNewFile();

                FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansChestFile);
                clansConfig.createSection("chest");
                clansConfig.save(clansChestFile);


            } catch (IOException e) {
                plugin.getLogger().severe("Could not create clans_chest.yml file: " + e.getMessage());
            }
        }
    }
}
