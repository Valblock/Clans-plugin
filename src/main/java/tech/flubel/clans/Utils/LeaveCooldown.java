package tech.flubel.clans.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Le delai d'attente entre deux clans, tenu par LuckPerms.
 *
 * <p>Quitter son camp ne doit pas etre gratuit : sans frein, un joueur suivrait
 * le camp qui mene, et le classement n'aurait plus aucun sens. Mais un premier
 * choix se fait mal informe - on decouvre ses amis, l'ambiance, le rythme - donc
 * UN changement est offert, une seule fois. Ensuite c'est une semaine d'attente.
 *
 * <p>POURQUOI LUCKPERMS PLUTOT QU'UN FICHIER A NOUS. Le delai est une permission
 * temporaire : LuckPerms sait deja les faire expirer tout seul, les stocke en
 * base, les partage entre serveurs et les rend lisibles par PlaceholderAPI. Un
 * fichier maison demanderait de reecrire tout cela, et se desynchroniserait du
 * reste des permissions du serveur.
 *
 *   clans.switch.used   permanente, posee au premier depart : le changement
 *                       offert a ete consomme
 *   clans.cooldown      temporaire, posee aux departs suivants, expire seule
 *
 * <p>La LECTURE passe par {@code hasPermission}, donc par Bukkit, sans dependre
 * de l'API LuckPerms. L'ECRITURE passe par la commande console, comme partout
 * ailleurs sur ce serveur (voir les recompenses de donjon).
 */
public class LeaveCooldown {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public LeaveCooldown(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("Leave_Cooldown.enabled", false);
    }

    private String duration() {
        return plugin.getConfig().getString("Leave_Cooldown.duration", "7d");
    }

    private String cooldownPermission() {
        return plugin.getConfig().getString("Leave_Cooldown.permission", "clans.cooldown");
    }

    private String usedPermission() {
        return plugin.getConfig().getString("Leave_Cooldown.free_switch_permission", "clans.switch.used");
    }

    private boolean freeSwitchEnabled() {
        return plugin.getConfig().getBoolean("Leave_Cooldown.free_switch", true);
    }

    /** Suffixe de contexte LuckPerms, vide si aucun serveur n'est configure. */
    private String context() {
        String server = plugin.getConfig().getString("Leave_Cooldown.server", "");
        return server.isEmpty() ? "" : " server=" + server;
    }

    /** Le joueur doit-il encore patienter avant de rejoindre un camp ? */
    public boolean isOnCooldown(Player player) {
        return isEnabled() && player.hasPermission(cooldownPermission());
    }

    /** Le changement offert est-il encore disponible ? */
    public boolean hasFreeSwitch(Player player) {
        return freeSwitchEnabled() && !player.hasPermission(usedPermission());
    }

    /**
     * A appeler APRES un depart reussi.
     *
     * <p>Consomme le changement offert s'il reste, sinon pose le delai. Dans les
     * deux cas le joueur est prevenu de ce qui vient de lui arriver - c'est tout
     * l'interet d'un frein annonce plutot que subi.
     */
    public void applyOnLeave(Player player) {
        if (!isEnabled()) return;

        if (hasFreeSwitch(player)) {
            dispatch("lp user " + player.getName() + " permission set "
                    + usedPermission() + " true" + context());
            tell(player, ChatColor.GREEN, languageManager.get("leave.free-switch-used"));
            return;
        }

        dispatch("lp user " + player.getName() + " permission settemp "
                + cooldownPermission() + " true " + duration() + context());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("duration", duration());
        tell(player, ChatColor.RED, languageManager.get("leave.cooldown-set", placeholders));
    }

    /** Le refus oppose a qui tente de rejoindre un camp trop tot. */
    public void denyJoin(org.bukkit.command.CommandSender notify) {
        if (notify == null) return;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("duration", duration());
        tell(notify, ChatColor.RED, languageManager.get("leave.cooldown-active", placeholders));
    }

    private void tell(org.bukkit.command.CommandSender who, ChatColor color, String message) {
        who.sendMessage(color + "" + ChatColor.BOLD
                + plugin.getConfig().getString("Plugin_Message_Indicator", "| ")
                + color + message);
    }

    private void dispatch(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
