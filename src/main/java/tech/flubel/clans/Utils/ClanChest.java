package tech.flubel.clans.Utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanChest {
    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public ClanChest(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    public void OpenInventory(Player player) {
        String clanName = getClanName(player);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanchest.not_in_clan"));
            return;
        }

        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        int chestSize = clansConfig.getInt("clans." + clanName + ".chest_size", 9);

        File clansChestFile = new File(plugin.getDataFolder(), "clans_chest.yml");
        FileConfiguration clansChestConfig = YamlConfiguration.loadConfiguration(clansChestFile);

        String ClansPrefix = clansConfig.getString("clans." + clanName + ".prefix");
        String formattedClanPrefix = formatClanPrefix(ClansPrefix);

        Inventory chestInventory = Bukkit.createInventory(null, chestSize, formattedClanPrefix + "'s" + ChatColor.BLACK + " Clan Chest");

        // Load saved items
        List<ItemStack> items = (List<ItemStack>) clansChestConfig.get("chest." + clanName);
        if (items != null) {
            for (int i = 0; i < items.size() && i < chestSize; i++) {
                chestInventory.setItem(i, items.get(i));
            }
        }

        player.openInventory(chestInventory);
    }

    public void UpgradeClanChest(Player player, Economy economy){
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        String clanName = getClanName(player);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanchest.not_in_clan"));
            return;
        }
        String currentLeader = clansConfig.getString("clans." + clanName + ".leader");

        if (!player.getName().equals(currentLeader)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanchest.leader-req"));
            return;
        }

        double requiredBalance = plugin.getConfig().getDouble("chest_upgrade_cost", 30000.0);

        if (economy.getBalance(player) < requiredBalance) {

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(requiredBalance));

            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanchest.low-amount", placeholders));
            return;
        }


        int chestSize = clansConfig.getInt("clans." + clanName + ".chest_size", 9);
        if(chestSize >= 54){
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanchest.max_size"));
            return;
        }

        clansConfig.set("clans." + clanName + ".chest_size", chestSize + 9);
        economy.withdrawPlayer(player, requiredBalance);

        try{
            clansConfig.save(clansFile);
            player.sendMessage(ChatColor.GREEN +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GREEN + languageManager.get("clanchest.success"));
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clanchest.error"));
        }

    }

    public String getClanName(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        if (!clansConfig.contains("clans")) return null;

        for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
            if (clansConfig.getString("clans." + clanName + ".leader").equals(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".co_leader").contains(player.getName()) ||
                    clansConfig.getStringList("clans." + clanName + ".members").contains(player.getName())) {
                return clanName;
            }
        }
        return null;
    }

    // Call this on InventoryCloseEvent
    public void saveInventory(String clanName, Inventory inventory) {
        File clansChestFile = new File(plugin.getDataFolder(), "clans_chest.yml");
        FileConfiguration clansChestConfig = YamlConfiguration.loadConfiguration(clansChestFile);

        clansChestConfig.set("chest." + clanName, inventory.getContents());

        try {
            clansChestConfig.save(clansChestFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save chest for clan " + clanName);
            e.printStackTrace();
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
