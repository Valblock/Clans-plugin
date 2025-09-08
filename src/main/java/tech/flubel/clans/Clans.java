package tech.flubel.clans;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;
import tech.flubel.clans.Utils.*;
import tech.flubel.clans.metrics.Metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public final class Clans extends JavaPlugin implements Listener {

    private Economy economy;
    private LanguageManager languageManager;


    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    private String latestVersion = null;
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault is not found or no economy plugin found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new CreateClanFolder(this);

        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("clan").setExecutor(this);
        this.getCommand("cc").setExecutor(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholderExpansion(this).register();
        }

        Metrics metrics = new Metrics(this,25416);



        saveDefaultConfig();


        saveLangFile("cn.yml");
        saveLangFile("de.yml");
        saveLangFile("en.yml");
        saveLangFile("fr.yml");
        saveLangFile("ru.yml");
        saveLangFile("tr.yml");


        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            latestVersion = checkForUpdate("https://api.spigotmc.org/legacy/update.php?resource=123903"); // replace with your URL
            if (latestVersion != null && !getDescription().getVersion().equalsIgnoreCase(latestVersion)) {
                getLogger().info("A new version is available: " + latestVersion);
            }
        });


        this.languageManager = new LanguageManager(this);


        getLogger().info("\u001B[38;2;23;138;214m================================================\u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m    ██████╗██╗      █████╗ ███╗   ██╗███████╗\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ██╔════╝██║     ██╔══██╗████╗  ██║██╔════╝\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ██║     ██║     ███████║██╔██╗ ██║███████╗\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ██║     ██║     ██╔══██║██║╚██╗██║╚════██║\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ╚██████╗███████╗██║  ██║██║ ╚████║███████║\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m    ╚═════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝\u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;225;215;0m                 Version: 1.6.0               \u001B[0m");
        getLogger().info("\u001B[38;2;0;255;0m                 Plugin Started               \u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m                (Made by Flubel)              \u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m================================================\u001B[0m");


        if (getServer().getPluginManager().isPluginEnabled("Fcore")) {
            getLogger().info("\u001B[38;2;0;255;0m Fcore detected, enabling Fcore integration...");
        } else {
            getLogger().warning("\u001B[38;2;255;0;0mFcore not found. Running in standalone mode.");
        }
    }

    private void saveLangFile(String fileName) {
        File file = new File(getDataFolder(), "lang/" + fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs(); // create lang/ directory if it doesn't exist
            saveResource("lang/" + fileName, false);
        }
    }

    public void setLanguageManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }


    @Override
    public void onDisable() {
//        saveConfig();
        getLogger().info("\u001B[38;2;23;138;214m================================================\u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m    ██████╗██╗      █████╗ ███╗   ██╗███████╗\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ██╔════╝██║     ██╔══██╗████╗  ██║██╔════╝\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ██║     ██║     ███████║██╔██╗ ██║███████╗\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ██║     ██║     ██╔══██║██║╚██╗██║╚════██║\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m   ╚██████╗███████╗██║  ██║██║ ╚████║███████║\u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m    ╚═════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝\u001B[0m");
        getLogger().info(" \u001B[0m");
         getLogger().info("\u001B[38;2;225;215;0m                 Version: 1.6.0               \u001B[0m");
           getLogger().info("\u001B[38;2;255;0;0m                 Plugin Stopped               \u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m                (Made by Flubel)              \u001B[0m");
        getLogger().info(" \u001B[0m");
        getLogger().info("\u001B[38;2;23;138;214m================================================\u001B[0m");
    }


    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/papi reload")) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                new ClanPlaceholderExpansion(this).register();
            }, 40L);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && latestVersion != null) {
            String current = getDescription().getVersion();
            if (!current.equalsIgnoreCase(latestVersion)) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "| [Clans] " + ChatColor.YELLOW + "A new version is available: " + latestVersion + " (you are on " + current + ")");
            }
            if (getServer().getPluginManager().isPluginEnabled("Fcore")) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "| [Clans] " + ChatColor.GREEN + "Fcore detected, Plugin will now work to its full extent.");
            } else {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "| [Clans] " + ChatColor.RED + "Fcore not found. Features like offline player kicking will not be enabled.");
            }
        }
    }

    private String checkForUpdate(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String version = reader.readLine().trim();
            reader.close();
            return version;
        } catch (Exception e) {
            getLogger().warning(languageManager.get("updater.failed-update-check") + ": " + e.getMessage());
            return null;
        }
    }


    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("clan.info.console"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.usage"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("cc")) {
            if (args.length < 1) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.short-chat"));
                return true;
            }
            String message = String.join(" ", args);
            ClanChat clanChat = new ClanChat(this, this.languageManager);
            clanChat.sendClanChatMessage(player,message);
            return true;
        }

        if (args[0].equalsIgnoreCase("chat")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.chat"));
                return true;
            }

            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            ClanChat clanChat = new ClanChat(this, this.languageManager);
            clanChat.sendClanChatMessage(player, message);
            return true;
        }

        switch (args[0]) {
            case "top":
                ListClans.ClanLister(player, languageManager, this);
                return true;
            case "create":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.name-req"));
                    return true;
                }
                if (args[1].length() >= 20) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.name-limit"));
                    return true;
                }
                String clanName = args[1];
                createClan(player, clanName);
                return true;
            case "invite": {
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.player-name"));
                    return true;
                }
                String playerNameToBeInvited = args[1];
                InvitePlayer invitePlayer = new InvitePlayer(this, this.languageManager);
                invitePlayer.sendInvite(player, playerNameToBeInvited);
                return true;
            }
            case "accept": {
                InvitePlayer invitePlayer = new InvitePlayer(this, this.languageManager);
                invitePlayer.AcceptInvite(player);
                return true;
            }
            case "deny": {
                InvitePlayer invitePlayer = new InvitePlayer(this, this.languageManager);
                invitePlayer.DenyInvite(player);
                return true;
            }
            case "info":
                ListPlayers listPlayers = new ListPlayers(this, this.languageManager);
                listPlayers.PlayerLister(player);
                return true;
            case "leave":
                LeaveClan leaveClan = new LeaveClan(this, this.languageManager);
                leaveClan.clanleaver(player);
                return true;
            case "promote":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.nprom"));
                    return true;
                }
                Promote promote = new Promote(this, this.languageManager);
                promote.promotePlayer(player, args[1]);
                return true;
            case "demote":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.ndemo"));
                    return true;
                }
                Demote demote = new Demote(this, this.languageManager);
                demote.demotePlayer(player, args[1]);
                return true;
            case "kick":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.mem-kick"));
                    return true;
                }
                Kick kick = new Kick(this, this.languageManager);
                kick.kickPlayer(player, args[1]);
                return true;
            case "transfer":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.transfer"));
                    return true;
                }
                TransferLeadership transferLeadership = new TransferLeadership(this, this.languageManager);
                transferLeadership.transferLeadership(player, args[1]);
                return true;
            case "join":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.null-clan"));
                    return true;
                }
                ClanJoin clanJoin = new ClanJoin(this, this.languageManager);
                clanJoin.requestJoinClan(player, args[1]);
                return true;
            case "raccept":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.naccept"));
                    return true;
                }
                AcceptorJoinReq acceptorJoinReq = new AcceptorJoinReq(this, this.languageManager);
                acceptorJoinReq.acceptJoinRequest(player, args[1]);
                return true;
            case "rdeny":
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.ndeny"));
                    return true;
                }
                DenyJoinReq denyJoinReq = new DenyJoinReq(this, this.languageManager);
                denyJoinReq.denyRequest(player, args[1]);
                return true;
            case "sethome":
                ClanHomeSet clanHomeSet = new ClanHomeSet(this, this.languageManager);
                clanHomeSet.setClanHome(player);
                return true;
            case "home":
                TpClanHome tpClanHome = new TpClanHome(this, this.languageManager);
                tpClanHome.teleportToClanHome(player);
                return true;
            case "requests":
                Requests requests = new Requests(this, this.languageManager);
                requests.showRequests(player);
                return true;
            case "reload":
                ReloadConfig reloadConfig = new ReloadConfig(this, this.languageManager);
                reloadConfig.reloadConfig(player);
                return true;
            case "upgrade":
                UpgradeClan upgradeClan = new UpgradeClan(this,this.economy, this.languageManager);
                try {
                    upgradeClan.ClanUpgrader(player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case "help":
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + languageManager.get("clan.clan_help_descriptions.title"));
                player.sendMessage(ChatColor.YELLOW + "/clan top " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.top"));
                player.sendMessage(ChatColor.YELLOW + "/clan create <name> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.create"));
                player.sendMessage(ChatColor.YELLOW + "/clan join <name> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.join"));
                player.sendMessage(ChatColor.GREEN + "/clan invite <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.invite"));
                player.sendMessage(ChatColor.GREEN + "/clan accept " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.accept"));
                player.sendMessage(ChatColor.GREEN + "/clan deny " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.deny"));
                player.sendMessage(ChatColor.GREEN + "/clan info " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.info"));
                player.sendMessage(ChatColor.GREEN + "/clan leave " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.leave"));
                player.sendMessage(ChatColor.GREEN + "/clan balance " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.balance"));
                player.sendMessage(ChatColor.GREEN + "/clan promote <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.promote"));
                player.sendMessage(ChatColor.GREEN + "/clan demote <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.demote"));
                player.sendMessage(ChatColor.GREEN + "/clan kick <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.kick"));
                player.sendMessage(ChatColor.GREEN + "/clan deposit <amount> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.deposit"));
                player.sendMessage(ChatColor.GREEN + "/clan withdraw <amount> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.withdraw"));
                player.sendMessage(ChatColor.GREEN + "/clan raccept <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.raccept"));
                player.sendMessage(ChatColor.GREEN + "/clan rdeny <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.rdeny"));
                player.sendMessage(ChatColor.GREEN + "/clan home " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.home"));
                player.sendMessage(ChatColor.GREEN + "/clan online " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.online"));
                player.sendMessage(ChatColor.GREEN + "/clan getbanner " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.getbanner"));
                player.sendMessage(ChatColor.GREEN + "/clan requests " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.requests"));
                player.sendMessage(ChatColor.GREEN + "/clan upgrade " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.upgrade"));
                player.sendMessage(ChatColor.GREEN + "/clan chest | /clan storage" + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.chest"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan pvp " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.pvp"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan transfer <player> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.transfer"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan togglejoin " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.togglejoin"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan sethome " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.sethome"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan change <new_prefix> " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.change"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan setbanner " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.setbanner"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan upgradestorage " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.upgradestorage"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan war | /clan enemy " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.enemy"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan ally " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.ally"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan unenemy " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.unenemy"));
                player.sendMessage(ChatColor.DARK_GREEN + "/clan unally " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.unally"));
                player.sendMessage(ChatColor.RED + "/clan reload " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.reload"));
                player.sendMessage(ChatColor.RED + "/clan delete " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.delete"));
                player.sendMessage(ChatColor.RED + "/clan updater " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.updater"));
                player.sendMessage(ChatColor.RED + "/clan updateconf " + ChatColor.WHITE + languageManager.get("clan.clan_help_descriptions.updateconf"));
                return true;
            case "pinfo":
                player.sendMessage(ChatColor.YELLOW + languageManager.get("clan.general"));
                player.sendMessage(ChatColor.YELLOW + languageManager.get("clan.version"));
                return true;
            case "deposit":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") +  ChatColor.WHITE + languageManager.get("clan.info.deposit"));
                    return true;
                }

                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") +  ChatColor.WHITE + languageManager.get("clan.info.bad-amount"));
                    return true;
                }

                ClanBankDeposit clanBankDeposit = new ClanBankDeposit(this, this.economy, this.languageManager);
                clanBankDeposit.BankClanDep(player, amount);
                return true;
            case "withdraw":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") +  ChatColor.WHITE + languageManager.get("clan.info.withdrawal"));
                    return true;
                }

                int amount1;
                try {
                    amount1 = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") +  ChatColor.WHITE + languageManager.get("clan.info.bad-amount"));
                    return true;
                }

                ClanBankWithdraw clanBankWithdraw = new ClanBankWithdraw(this, this.economy, this.languageManager);
                clanBankWithdraw.withdrawFromClan(player, amount1);
                return true;
            case "pvp":
                ClanPvPToggle clanPvPToggle = new ClanPvPToggle(this, this.languageManager);
                clanPvPToggle.pvptoggler(player);
                return true;
            case "balance":
                ClanBalanceViewer clanBalanceViewer = new ClanBalanceViewer(this, this.languageManager);
                clanBalanceViewer.balanceviewer(player);
                return true;
            case "delete":
                DeleteClan deleteClan = new DeleteClan(this, this.languageManager);
                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.delerror"));
                    return true;
                }

                deleteClan.deleteclan(args[1].toLowerCase(), player);
                return true;
            case "change":
                ChangePrefix changePrefix = new ChangePrefix(this, this.languageManager);

                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("clan.info.no-name"));
                    return true;
                }

                try {
                    changePrefix.ChangeClanName(args[1], player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case "online":
                ClanOnline clanOnline = new ClanOnline(this, this.languageManager);
                clanOnline.FetchOnlineMembers(player);
                return true;
            case "togglejoin":
                JoinToggler joinToggler = new JoinToggler(this, this.languageManager);
                joinToggler.JoinTogglerFunc(player);
                return true;
            case "updater":
                if (player.hasPermission("clans.admin") && latestVersion != null) {
                    String current = getDescription().getVersion();
                    if (!current.equalsIgnoreCase(latestVersion)) {
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.YELLOW + languageManager.get("updater.new-version") + latestVersion + " (" + languageManager.get("updater.current-version") + current + ")");
                    }else {
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.YELLOW + languageManager.get("updater.latest-version"));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clans.info.no-perm"));
                }
                return true;
            case "setbanner":
                SetBanner setBanner = new SetBanner(this, this.languageManager);
                setBanner.SetClanBanner(player);
                return true;
            case "getbanner":
                SetBanner setBanner1 = new SetBanner(this, this.languageManager);
                setBanner1.getClanBanner(player);
                return true;
            case "war":
            case "enemy":
                EnemiesorAllies enemiesorAllies = new EnemiesorAllies(this, this.languageManager);

                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("enemies.no-name"));
                    return true;
                }
                enemiesorAllies.SetEnemies(player, args[1]);

                return true;
            case "ally":
                EnemiesorAllies enemiesorAllies1 = new EnemiesorAllies(this, this.languageManager);

                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("allies.no-name"));
                    return true;
                }
                enemiesorAllies1.SetAllies(player, args[1]);

                return true;
            case "unenemy":
                EnemiesorAllies enemiesorAllies3 = new EnemiesorAllies(this, this.languageManager);

                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("enemies.no-name"));
                    return true;
                }
                enemiesorAllies3.RemEnemies(player, args[1]);

                return true;
            case "unally":
                EnemiesorAllies enemiesorAllies4 = new EnemiesorAllies(this, this.languageManager);

                if (args.length < 2 || args[1].isEmpty()) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.WHITE + languageManager.get("allies.no-name"));
                    return true;
                }
                enemiesorAllies4.RemAllies(player, args[1]);

                return true;
            case "chest":
            case "storage":
                ClanChest clanChest = new ClanChest(this, this.languageManager);
                clanChest.OpenInventory(player);

                return true;
            case "upgradestorage":
                ClanChest clanChestUpg = new ClanChest(this, this.languageManager);
                clanChestUpg.UpgradeClanChest(player, economy);

                return true;
            case "updateconf":
                UpdateConfig updateConfig = new UpdateConfig(this, this.languageManager);
                if (player.hasPermission("clans.admin")) {
                    updateConfig.fixClansConfig(player);
                } else {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clans.info.no-perm"));
                }

        }


        return true;
    }




    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        File clansFile = new File(this.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

        if (cmd.getName().equalsIgnoreCase("clan")) {

            Player player = (Player) sender;

            List<String> suggestions = new ArrayList<>();
            List<String> subCommands = Arrays.asList("updateconf", "upgradestorage","storage", "chest", "unally", "unenemy", "ally" ,"war", "enemy" ,"setbanner", "getbanner", "togglejoin","online", "updater", "chat", "top", "create", "invite", "accept", "deny", "info", "leave", "promote", "demote", "kick", "transfer", "join", "raccept", "rdeny", "sethome", "home", "requests", "reload", "upgrade", "help", "pinfo", "deposit", "withdraw", "pvp", "balance", "delete", "change");


            if (args.length == 1) {
                String input = args[0].toLowerCase();
                for (String subCommand : subCommands) {
                    if (subCommand.startsWith(input)) {
                        suggestions.add(subCommand);
                    }
                }
                return suggestions;
            }else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("war") || args[0].equalsIgnoreCase("ally") || args[0].equalsIgnoreCase("join")  ) {
                    if (config.contains("clans")) {
                        Set<String> clanNames = config.getConfigurationSection("clans").getKeys(false);
                        for (String clan : clanNames) {
                            if (clan.toLowerCase().startsWith(args[1].toLowerCase())) {
                                suggestions.add(clan);
                            }
                        }
                        return suggestions;
                    }
                }
                if (args[0].equalsIgnoreCase("unenemy")) {
                    String ClanName = getClanName(player);
                    List<String> ClanEnemies = config.getStringList("clans." + ClanName  + ".enemies");

                    for (String clan : ClanEnemies) {
                        if (clan.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(clan);
                        }
                    }
                    return suggestions;

                }
                if (args[0].equalsIgnoreCase("unally")) {
                    String ClanName = getClanName(player);
                    List<String> ClanAllies = config.getStringList("clans." + ClanName  + ".allies");

                    for (String clan : ClanAllies) {
                        if (clan.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(clan);
                        }
                    }
                    return suggestions;

                }
            }

        }
        return null;
    }



    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        String victimClan = getClanName(victim);
        String attackerClan = getClanName(attacker);

        if (victimClan == null || attackerClan == null) return;

        File clansFile = new File(this.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        boolean cancelAttack = false;

        // Same clan
        if (victimClan.equals(attackerClan)) {
            cancelAttack = !clansConfig.getBoolean("clans." + victimClan + ".pvp", true);
        }

        // Allied clans (MUST be mutual)
        else {
            List<String> victimAllies = clansConfig.getStringList("clans." + victimClan + ".allies");
            List<String> attackerAllies = clansConfig.getStringList("clans." + attackerClan + ".allies");

            boolean mutuallyAllied = victimAllies.contains(attackerClan) && attackerAllies.contains(victimClan);

            if (mutuallyAllied) {
                boolean victimPvp = clansConfig.getBoolean("clans." + victimClan + ".pvp", true);
                boolean attackerPvp = clansConfig.getBoolean("clans." + attackerClan + ".pvp", true);

                // block attack if either clan disables pvp
                if (!victimPvp || !attackerPvp) {
                    cancelAttack = true;
                }
            }
        }

        if (cancelAttack) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + languageManager.get("pvp.attacker-msg"));

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("attacker", attacker.getName());
            victim.sendMessage(ChatColor.RED + languageManager.get("pvp.victim-msg", placeholders));
        }
    }


    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        ClanChest clanChest = new ClanChest(this, this.languageManager);
        String clanName = clanChest.getClanName(player);

        if (clanName == null) return;

        File clansFile = new File(this.getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        String ClansPrefix = clansConfig.getString("clans." + clanName + ".prefix", clanName);
        String formattedClanPrefix = formatClanPrefix(ClansPrefix);

        String expectedTitle = formattedClanPrefix + "'s" + ChatColor.BLACK + " Clan Chest";

        if (ChatColor.stripColor(event.getView().getTitle())
                .equals(ChatColor.stripColor(expectedTitle))) {
            clanChest.saveInventory(clanName, event.getInventory());
        }
    }



    @EventHandler
    public void onPlayerKills(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        String victimsClan = getClanName(victim);
        String killersClan = killer != null ? getClanName(killer) : null;

        File clansFile = new File(this.getDataFolder(), "clans.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(clansFile);

        // Victim's clan: add death
        if (victimsClan != null) {
            int currentDeaths = config.getInt("clans." + victimsClan + ".deaths", 0);
            config.set("clans." + victimsClan + ".deaths", currentDeaths + 1);
        }

        // Killer's clan: add kill
        if (killer != null && !killer.equals(victim) && killersClan != null) {
            int currentKills = config.getInt("clans." + killersClan + ".kills", 0);
            config.set("clans." + killersClan + ".kills", currentKills + 1);
        }

        try {
            config.save(clansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getClanName(Player player) {
        File clansFile = new File(this.getDataFolder(), "clans.yml");
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


    public void createClan(Player player, String clanName) {
        double requiredBalance = this.getConfig().getDouble("Amount", 50000.0);

        SearchPlayer searchPlayer = new SearchPlayer(this);

        if (searchPlayer.isPlayerInClan(player)) {
            player.sendMessage(ChatColor.RED + "" +ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clan.error.already-in-clan"));
            return;
        }

        if (economy.getBalance(player) < requiredBalance) {
            double amount = this.getConfig().getDouble("Amount", 50000.0);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount));

            player.sendMessage(ChatColor.RED +""+ ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clan.error.low-amount", placeholders));
            return;
        }

        File clansFile = new File(getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);


        File clansChestFile = new File(getDataFolder(), "clans_chest.yml");
        FileConfiguration clansChestConfig = YamlConfiguration.loadConfiguration(clansChestFile);

        String cleanedClanName = clanName.replaceAll("&[a-zA-Z0-9]", "").replaceAll("&#[a-fA-F0-9]{6}", "").toLowerCase();

        Set<String> existingClanNames = clansConfig.getConfigurationSection("clans").getKeys(false);

        for (String existingClan : existingClanNames) {
            String cleanedExistingClanName = existingClan.replaceAll("&[a-zA-Z0-9]", "").replaceAll("&#[a-fA-F0-9]{6}", "").toLowerCase();

            if (cleanedClanName.equals(cleanedExistingClanName)) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clan.error.name-taken"));
                return;
            }
        }


        Set<String> BannedPrefixes =  new HashSet<>(this.getConfig().getStringList("banned_prefixes"));

        for (String BannedPrefix : BannedPrefixes) {
            String cleanedExistingClanName = BannedPrefix.toLowerCase();

            if (cleanedClanName.equals(cleanedExistingClanName)) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clan.error.banned-prefix"));
                return;
            }
        }

        if(cleanedClanName.contains(" ")){
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.RED + languageManager.get("clan.error.no-space"));
            return;
        }


        economy.withdrawPlayer(player, requiredBalance);

        clansConfig.set("clans." + cleanedClanName + ".leader", player.getName());
        clansConfig.set("clans." + cleanedClanName + ".co_leader", new ArrayList<>());
        clansConfig.set("clans." + cleanedClanName + ".members", new ArrayList<>());
        clansConfig.set("clans." + cleanedClanName + ".prefix", clanName);
        clansConfig.set("clans." + cleanedClanName + ".pvp", true);
        clansConfig.set("clans." + cleanedClanName + ".joins", true);
        clansConfig.set("clans." + cleanedClanName + ".balance", this.getConfig().getInt("initial_balance",25000));
        clansConfig.set("clans." + cleanedClanName + ".max_members", this.getConfig().getInt("max_members",50));
        clansConfig.set("clans." + cleanedClanName + ".prefix_change", this.getConfig().getInt("prefix_change",1));
        clansConfig.set("clans." + cleanedClanName + ".enemies", new ArrayList<>());
        clansConfig.set("clans." + cleanedClanName + ".allies", new ArrayList<>());
        clansConfig.set("clans." + cleanedClanName + ".kills", player.getStatistic(Statistic.PLAYER_KILLS));
        clansConfig.set("clans." + cleanedClanName + ".deaths", player.getStatistic(Statistic.DEATHS));
        clansConfig.set("clans." + cleanedClanName + ".chest_size", 9);
        clansChestConfig.set("chest." + cleanedClanName, new ArrayList<>());


// May add in the future :p
//        clansConfig.set("clans." + cleanedClanName + ".id", generateClanId(7));

        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(languageManager.get("clan.error.save-clan"));
            return;
        }
        String TranslatedClanName = formatClanPrefix(clanName);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("clan_name", TranslatedClanName);

        String message = languageManager.get("clan.success.created", placeholders);
        message = message.replace(TranslatedClanName, TranslatedClanName + ChatColor.GREEN);
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + this.getConfig().getString("Plugin_Message_Indicator", "| ") + ChatColor.GREEN + message);
    }


    private static String formatClanPrefix(String prefix) {
        if (prefix == null) return "";
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



// May add in the future :p
//    public static String generateClanId(int length) {
//        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//        StringBuilder id = new StringBuilder();
//        Random random = new Random();
//        for (int i = 0; i < length; i++) {
//            id.append(chars.charAt(random.nextInt(chars.length())));
//        }
//        return id.toString();
//    }

}
