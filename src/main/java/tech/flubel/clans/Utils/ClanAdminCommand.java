package tech.flubel.clans.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * /clanadmin - la porte d'entree CONSOLE du plugin.
 *
 * <p>Toutes les sous-commandes de /clan refusent la console : onCommand
 * commence par un {@code sender instanceof Player} et repond
 * "clan.info.console". C'est coherent pour un plugin ou chaque clan a un chef
 * humain, et bloquant pour un serveur ou les clans appartiennent au serveur :
 * aucun automate ne peut alors placer un joueur.
 *
 * <p>Cette commande est volontairement SEPAREE de /clan plutot qu'ajoutee
 * comme sous-commande : /clan garde son garde-fou console intact, et la
 * surface d'administration est protegee par une permission distincte.
 */
public class ClanAdminCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public ClanAdminCommand(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    private void reply(CommandSender sender, ChatColor color, String message) {
        sender.sendMessage(color + "" + ChatColor.BOLD
                + plugin.getConfig().getString("Plugin_Message_Indicator", "| ")
                + color + message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clans.admin")) {
            reply(sender, ChatColor.RED, languageManager.get("clan.info.no-perm"));
            return true;
        }

        if (args.length < 1) {
            reply(sender, ChatColor.GOLD, languageManager.get("admin.usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "autojoin": {
                if (args.length < 2) {
                    reply(sender, ChatColor.GOLD, languageManager.get("admin.usage"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    reply(sender, ChatColor.RED, languageManager.get("admin.offline"));
                    return true;
                }
                AutoBalance autoBalance = new AutoBalance(plugin, languageManager);
                if (!autoBalance.isEnabled()) {
                    reply(sender, ChatColor.RED, languageManager.get("autobalance.not-configured"));
                    return true;
                }
                autoBalance.assign(target, sender);
                return true;
            }

            case "add": {
                if (args.length < 3) {
                    reply(sender, ChatColor.GOLD, languageManager.get("admin.usage"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    reply(sender, ChatColor.RED, languageManager.get("admin.offline"));
                    return true;
                }
                String clanName = args[2];
                if (!clanExists(clanName)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("clan_name", clanName);
                    reply(sender, ChatColor.RED, languageManager.get("autobalance.missing-clan", placeholders));
                    return true;
                }
                SearchPlayer searchPlayer = new SearchPlayer(plugin);
                if (searchPlayer.isPlayerInClan(target)) {
                    reply(sender, ChatColor.RED, languageManager.get("join.already-member"));
                    return true;
                }
                new AddPlayer(plugin, languageManager).PlayerAdder(clanName, target);
                reply(sender, ChatColor.GREEN, languageManager.get("admin.added"));
                return true;
            }

            // Retrait force, y compris pour un joueur hors ligne : c'est le seul
            // moyen de reparer clans.yml sans arreter le serveur. Le chef n'est
            // jamais retire - il faut passer par /clan transfer d'abord, sinon
            // le clan se retrouverait sans proprietaire.
            case "remove": {
                if (args.length < 2) {
                    reply(sender, ChatColor.GOLD, languageManager.get("admin.usage"));
                    return true;
                }
                String name = args[1];
                File clansFile = new File(plugin.getDataFolder(), "clans.yml");
                FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);
                if (!clansConfig.contains("clans")) {
                    reply(sender, ChatColor.RED, languageManager.get("admin.not-in-clan"));
                    return true;
                }

                boolean removed = false;
                for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
                    String leader = clansConfig.getString("clans." + clanName + ".leader");
                    if (name.equalsIgnoreCase(leader)) {
                        reply(sender, ChatColor.RED, languageManager.get("admin.is-leader"));
                        return true;
                    }
                    List<String> members = clansConfig.getStringList("clans." + clanName + ".members");
                    List<String> coLeaders = clansConfig.getStringList("clans." + clanName + ".co_leader");
                    boolean inMembers = members.removeIf(name::equalsIgnoreCase);
                    boolean inCoLeaders = coLeaders.removeIf(name::equalsIgnoreCase);
                    if (inMembers || inCoLeaders) {
                        clansConfig.set("clans." + clanName + ".members", members);
                        clansConfig.set("clans." + clanName + ".co_leader", coLeaders);
                        removed = true;
                    }
                }

                if (!removed) {
                    reply(sender, ChatColor.RED, languageManager.get("admin.not-in-clan"));
                    return true;
                }
                try {
                    clansConfig.save(clansFile);
                    reply(sender, ChatColor.GREEN, languageManager.get("admin.removed"));
                } catch (Exception e) {
                    reply(sender, ChatColor.RED, languageManager.get("add.error"));
                    e.printStackTrace();
                }
                return true;
            }

            // Diagnostic : l'etat de l'equilibre, en une ligne par camp.
            case "balance": {
                AutoBalance autoBalance = new AutoBalance(plugin, languageManager);
                List<String> clans = autoBalance.getClans();
                if (clans.isEmpty()) {
                    reply(sender, ChatColor.RED, languageManager.get("autobalance.not-configured"));
                    return true;
                }
                MemberCount memberCount = new MemberCount(plugin);
                reply(sender, ChatColor.GOLD, languageManager.get("admin.balance-header"));
                for (String clanName : clans) {
                    sender.sendMessage(ChatColor.GRAY + "  " + clanName + ": "
                            + ChatColor.WHITE + memberCount.getClanMembersCount(clanName));
                }
                sender.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.WHITE + autoBalance.pickClan());
                return true;
            }

            default:
                reply(sender, ChatColor.GOLD, languageManager.get("admin.usage"));
                return true;
        }
    }

    private boolean clanExists(String clanName) {
        File clansFile = new File(plugin.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        return clansConfig.contains("clans." + clanName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("clans.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("autojoin", "add", "remove", "balance").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("balance")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            File clansFile = new File(plugin.getDataFolder(), "clans.yml");
            FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);
            if (clansConfig.getConfigurationSection("clans") == null) return new ArrayList<>();
            return clansConfig.getConfigurationSection("clans").getKeys(false).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
