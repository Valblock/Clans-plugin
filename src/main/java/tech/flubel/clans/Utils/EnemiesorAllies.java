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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnemiesorAllies {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public EnemiesorAllies(JavaPlugin plugin, LanguageManager languageManager){
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    public void SetAllies(Player player, String ClanAlly) {

            File clansFile = new File(plugin.getDataFolder(), "clans.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

            String clanName = getClanName(player);
            if (clanName == null) {
                player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("allies.no-clan-self"));
                return;
            }

            if (!config.contains("clans." + ClanAlly)) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("allies.no-clan"));
                return;
            }

            String currentLeader = config.getString("clans." + clanName + ".leader");

            if (!player.getName().equals(currentLeader)) {
                player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("allies.leader-req"));
                return;
            }

            if (clanName.equalsIgnoreCase(ClanAlly)) {
                player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("allies.own-clan"));
                return;
            }


            List<String> allies = config.getStringList("clans." + clanName + ".allies");
            List<String> enemies = config.getStringList("clans." + clanName + ".enemies");

            if(enemies.contains(ClanAlly)){
                player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("allies.clan-enem"));
                return;
            }

            if (!allies.contains(ClanAlly)) {
                allies.add(ClanAlly);
            }
            config.set("clans." + clanName + ".allies", allies);

            List<String> clanMembers = getClanMembers(clanName);

            for (String memberName : clanMembers) {
                Player member = Bukkit.getPlayer(memberName);
                if (member != null && member.isOnline()) {

                    Map<String, String> placeholders = new HashMap<>();

                    String AllyClansPrefix = config.getString("clans." + ClanAlly.toLowerCase() + ".prefix");
                    String formattedPrefix = formatClanPrefix(AllyClansPrefix);

                    formattedPrefix = formattedPrefix + ChatColor.GREEN;
                    placeholders.put("ally", formattedPrefix);

                    member.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("allies.success", placeholders));
                }
            }

            List<String> AllyclanMembers = getClanMembers(ClanAlly);

            for (String memberName : AllyclanMembers) {
                Player member = Bukkit.getPlayer(memberName);
                if (member != null && member.isOnline()) {

                    Map<String, String> placeholders = new HashMap<>();

                    String AllyClansPrefix1 = config.getString("clans." + clanName.toLowerCase() + ".prefix");
                    String formattedPrefix = formatClanPrefix(AllyClansPrefix1);

                    formattedPrefix = formattedPrefix + ChatColor.GREEN;
                    placeholders.put("ally", formattedPrefix);

                    member.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("allies.success_other_clan", placeholders));
                }
            }


            try {
                config.save(clansFile);
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("allies.error"));
                e.printStackTrace();
            }


    }

    public void SetEnemies(Player player, String ClanEnem) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

        String clanName = getClanName(player);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("enemies.no-clan-self"));
            return;
        }

        if (!config.contains("clans." + ClanEnem)) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("enemies.no-clan"));
            return;
        }

        String currentLeader = config.getString("clans." + clanName + ".leader");

        if (!player.getName().equals(currentLeader)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("enemies.leader-req"));
            return;
        }

        if (clanName.equalsIgnoreCase(ClanEnem)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("enemies.own-clan"));
            return;
        }


        List<String> enemies = config.getStringList("clans." + clanName + ".enemies");
        List<String> allies = config.getStringList("clans." + clanName + ".allies");
        if (allies.contains(ClanEnem)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("enemies.clan-ally"));
            return;
        }

        if (!enemies.contains(ClanEnem)) {
            enemies.add(ClanEnem);
        }
        config.set("clans." + clanName + ".enemies", enemies);

        List<String> clanMembers = getClanMembers(clanName);

        for (String memberName : clanMembers) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {

                Map<String, String> placeholders = new HashMap<>();

                String enemyClansPrefix = config.getString("clans." + ClanEnem.toLowerCase() + ".prefix");
                String formattedPrefix = formatClanPrefix(enemyClansPrefix);

                formattedPrefix = formattedPrefix + ChatColor.GREEN;

                placeholders.put("enemy", formattedPrefix);

                member.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GREEN + languageManager.get("enemies.success", placeholders));
            }
        }

        List<String> AllyclanMembers = getClanMembers(ClanEnem);

        for (String memberName : AllyclanMembers) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {

                Map<String, String> placeholders = new HashMap<>();

                String EnemClansPrefix1 = config.getString("clans." + clanName.toLowerCase() + ".prefix");
                String formattedPrefix = formatClanPrefix(EnemClansPrefix1);

                formattedPrefix = formattedPrefix + ChatColor.GREEN;
                placeholders.put("enemy", formattedPrefix);

                member.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("enemies.success_other_clan", placeholders));
            }
        }

        try {
            config.save(clansFile);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("enemies.error"));
            e.printStackTrace();
        }


    }











    public void RemAllies(Player player, String ClanAlly) {

        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

        String clanName = getClanName(player);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remallies.no-clan-self"));
            return;
        }

        if (!config.contains("clans." + clanName)) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.no-clan"));
            return;
        }

        String currentLeader = config.getString("clans." + clanName + ".leader");

        if (!player.getName().equals(currentLeader)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remallies.leader-req"));
            return;
        }

        if (clanName.equalsIgnoreCase(ClanAlly)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remallies.own-clan"));
            return;
        }


        List<String> allies = config.getStringList("clans." + clanName + ".allies");
        if (!allies.contains(ClanAlly)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remallies.not-allied"));
            return;
        }else{
            allies.remove(ClanAlly);
        }
        config.set("clans." + clanName + ".allies", allies);

        List<String> clanMembers = getClanMembers(clanName);

        for (String memberName : clanMembers) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {

                Map<String, String> placeholders = new HashMap<>();

                String enemyClansPrefix = config.getString("clans." + ClanAlly.toLowerCase() + ".prefix");
                String formattedPrefix = formatClanPrefix(enemyClansPrefix);

                formattedPrefix = formattedPrefix + ChatColor.GREEN;
                placeholders.put("ally", formattedPrefix);

                member.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("remallies.success", placeholders));
            }
        }

        List<String> AllyclanMembers = getClanMembers(ClanAlly);

        for (String memberName : AllyclanMembers) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {

                Map<String, String> placeholders = new HashMap<>();

                String AllyClansPrefix1 = config.getString("clans." + clanName.toLowerCase() + ".prefix");
                String formattedPrefix = formatClanPrefix(AllyClansPrefix1);

                formattedPrefix = formattedPrefix + ChatColor.GREEN;
                placeholders.put("ally", formattedPrefix);

                member.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("remallies.success_other_clan", placeholders));
            }
        }
        try {
            config.save(clansFile);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remallies.error"));
            e.printStackTrace();
        }


    }

    public void RemEnemies(Player player, String ClanEnem) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

        String clanName = getClanName(player);
        if (clanName == null) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.no-clan-self"));
            return;
        }

        if (!config.contains("clans." + clanName)) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.no-clan"));
            return;
        }

        String currentLeader = config.getString("clans." + clanName + ".leader");

        if (!player.getName().equals(currentLeader)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.leader-req"));
            return;
        }

        if (clanName.equalsIgnoreCase(ClanEnem)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.own-clan"));
            return;
        }


        List<String> enemies = config.getStringList("clans." + clanName + ".enemies");
        if (!enemies.contains(ClanEnem)) {
            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.not-enemies"));
            return;
        } else {
            enemies.remove(ClanEnem);
        }
        config.set("clans." + clanName + ".enemies", enemies);

        List<String> clanMembers = getClanMembers(clanName);

        for (String memberName : clanMembers) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {

                Map<String, String> placeholders = new HashMap<>();

                String enemyClansPrefix = config.getString("clans." + ClanEnem.toLowerCase() + ".prefix");
                String formattedPrefix = formatClanPrefix(enemyClansPrefix);

                formattedPrefix = formattedPrefix + ChatColor.GREEN;

                placeholders.put("enemy", formattedPrefix);

                member.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GREEN + languageManager.get("remenemies.success", placeholders));
            }
        }


        List<String> EnemclanMembers = getClanMembers(ClanEnem);

        for (String memberName : EnemclanMembers) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {

                Map<String, String> placeholders = new HashMap<>();

                String EnemClansPrefix1 = config.getString("clans." + clanName.toLowerCase() + ".prefix");
                String formattedPrefix = formatClanPrefix(EnemClansPrefix1);

                formattedPrefix = formattedPrefix + ChatColor.GREEN;
                placeholders.put("enemy", formattedPrefix);

                member.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GOLD + languageManager.get("remenemies.success_other_clan", placeholders));
            }
        }
        try {
            config.save(clansFile);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + plugin.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("remenemies.error"));
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

    private String getClanName(Player player) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

        for (String clanName : config.getConfigurationSection("clans").getKeys(false)) {
            List<String> members = config.getStringList("clans." + clanName + ".members");
            List<String> coLeaders = config.getStringList("clans." + clanName + ".co_leader");
            String leader = config.getString("clans." + clanName + ".leader");

            if (members.contains(player.getName()) || coLeaders.contains(player.getName()) || player.getName().equals(leader)) {
                return clanName;
            }
        }
        return null;
    }


    private List<String> getClanMembers(String clanName) {
        List<String> members = new ArrayList<>();
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        if (clansConfig.contains("clans." + clanName + ".members")) {
            members.addAll(clansConfig.getStringList("clans." + clanName + ".members"));
        }
        if (clansConfig.contains("clans." + clanName + ".leader")) {
            members.add(clansConfig.getString("clans." + clanName + ".leader"));
        }
        if (clansConfig.contains("clans." + clanName + ".co_leader")) {
            members.addAll(clansConfig.getStringList("clans." + clanName + ".co_leader"));
        }

        return members;
    }
}
