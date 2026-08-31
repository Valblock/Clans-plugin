package tech.flubel.clans.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Equilibrage des camps par PLAFOND D'ECART.
 *
 * <p>Le plugin d'origine part du principe qu'un clan appartient a un joueur :
 * on cree, on invite, un chef accepte. Sur un serveur ou les clans sont des
 * CAMPS FIXES appartenant au serveur, il n'y a personne pour accepter, et les
 * demandes s'empilent indefiniment.
 *
 * <p>LE JOUEUR CHOISIT SON CAMP - c'est un engagement, pas un tirage - mais il
 * ne peut pas creuser l'ecart indefiniment. Un camp est ferme des qu'il compte
 * {@code max_gap} membres de plus que l'autre : la marge est toujours d'au plus
 * trois places, et le camp minoritaire reste toujours ouvert.
 *
 * <p>Une premiere version attribuait le camp d'office, au plus petit effectif.
 * L'ecart n'y depassait jamais un membre, mais le joueur subissait son camp au
 * lieu de le rejoindre. Le plafond garde la garantie d'equilibre en rendant la
 * decision au joueur.
 *
 * <p>Les effectifs sont relus dans clans.yml A CHAQUE APPEL, jamais gardes dans
 * un compteur a part : un depart par /clan leave ou /clan kick rouvre donc le
 * camp tout seul, sans aucune commande d'administration.
 */
public class AutoBalance {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public AutoBalance(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    /** L'equilibrage est-il actif et correctement configure ? */
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("Auto_Balance.enabled", false)
                && getClans().size() >= 2;
    }

    public List<String> getClans() {
        return plugin.getConfig().getStringList("Auto_Balance.clans");
    }

    /** Ecart maximal tolere entre le camp le plus fourni et le moins fourni. */
    public int getMaxGap() {
        return Math.max(1, plugin.getConfig().getInt("Auto_Balance.max_gap", 3));
    }

    public int getMemberCount(String clanName) {
        return new MemberCount(plugin).getClanMembersCount(clanName);
    }

    /** Effectif du camp le MOINS fourni, celui qui sert de reference. */
    private int smallestCount() {
        int smallest = Integer.MAX_VALUE;
        for (String clanName : getClans()) {
            smallest = Math.min(smallest, getMemberCount(clanName));
        }
        return smallest == Integer.MAX_VALUE ? 0 : smallest;
    }

    /**
     * Places restantes avant que ce camp n'atteigne le plafond.
     *
     * <p>Zero signifie ferme. Le camp le moins fourni rend toujours au moins
     * {@code max_gap}, donc il n'est jamais possible de tout verrouiller.
     */
    public int remainingSlots(String clanName) {
        if (!isEnabled()) return Integer.MAX_VALUE;
        int gap = getMemberCount(clanName) - smallestCount();
        return Math.max(0, getMaxGap() - gap);
    }

    public boolean canJoin(String clanName) {
        return remainingSlots(clanName) > 0;
    }

    /** Le camp le moins fourni - utilise par /clanadmin autojoin. */
    public String pickClan() {
        String best = null;
        int bestCount = Integer.MAX_VALUE;
        for (String clanName : getClans()) {
            int count = getMemberCount(clanName);
            if (count < bestCount) {
                bestCount = count;
                best = clanName;
            }
        }
        return best;
    }

    /**
     * Place un joueur dans un camp donne, en verifiant le plafond.
     *
     * @param player   le joueur a placer, qui doit etre en ligne
     * @param clanName le camp vise ; null pour laisser le moins fourni
     * @param notify   destinataire des refus ; peut etre null
     * @return true si le joueur a bien ete place
     */
    public boolean join(Player player, String clanName, org.bukkit.command.CommandSender notify) {
        SearchPlayer searchPlayer = new SearchPlayer(plugin);
        if (searchPlayer.isPlayerInClan(player)) {
            deny(notify, languageManager.get("join.already-member"));
            return false;
        }

        // Le delai d'apres-depart passe AVANT le plafond : un joueur qui vient
        // de quitter doit s'entendre dire qu'il doit patienter, pas que le camp
        // est complet.
        LeaveCooldown cooldown = new LeaveCooldown(plugin, languageManager);
        if (cooldown.isOnCooldown(player)) {
            cooldown.denyJoin(notify);
            return false;
        }

        if (clanName == null) clanName = pickClan();
        if (clanName == null) {
            deny(notify, languageManager.get("autobalance.not-configured"));
            return false;
        }

        // On retrouve l'orthographe declaree en configuration : le joueur tape
        // "kattegat", clans.yml connait "Kattegat".
        for (String declared : getClans()) {
            if (declared.equalsIgnoreCase(clanName)) {
                clanName = declared;
                break;
            }
        }

        if (!clanExists(clanName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("clan_name", clanName);
            deny(notify, languageManager.get("autobalance.missing-clan", placeholders));
            return false;
        }

        if (!canJoin(clanName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("clan_name", clanName);
            placeholders.put("gap", String.valueOf(getMaxGap()));
            deny(notify, languageManager.get("autobalance.clan-full", placeholders));
            return false;
        }

        // PlayerAdder ecrit dans clans.yml, controle max_members et previent le
        // joueur lui-meme. Rien de tout cela n'est reecrit ici.
        new AddPlayer(plugin, languageManager).PlayerAdder(clanName, player);
        return true;
    }

    private void deny(org.bukkit.command.CommandSender notify, String message) {
        if (notify == null) return;
        notify.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
                + plugin.getConfig().getString("Plugin_Message_Indicator", "| ")
                + ChatColor.RED + message);
    }

    private boolean clanExists(String clanName) {
        java.io.File clansFile = new java.io.File(plugin.getDataFolder(), "clans.yml");
        org.bukkit.configuration.file.FileConfiguration clansConfig =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(clansFile);
        return clansConfig.contains("clans." + clanName);
    }
}
