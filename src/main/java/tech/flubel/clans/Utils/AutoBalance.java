package tech.flubel.clans.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.flubel.clans.LanguageManager.LanguageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attribution automatique et equilibree d'un joueur a l'un des clans declares.
 *
 * <p>Le plugin d'origine part du principe qu'un clan appartient a un joueur :
 * on cree, on invite, un chef accepte. Sur un serveur ou les clans sont des
 * CAMPS FIXES appartenant au serveur lui-meme, ce modele n'a personne pour
 * accepter les demandes - elles s'empilent indefiniment.
 *
 * <p>Cette classe repond a ce cas : le joueur ne choisit pas son camp, et
 * personne n'a a valider. Le camp le moins peuple l'emporte, ce qui garantit
 * un ecart d'au plus un membre. En cas d'egalite, c'est l'ordre de la liste de
 * configuration qui tranche - deterministe, donc testable.
 *
 * <p>Le comptage est fait a chaque appel sur l'etat reel de clans.yml, pas sur
 * un compteur tenu a part. C'est ce qui rend l'equilibre AUTO-CORRECTIF : un
 * depart par /clan leave ou /clan kick creuse un ecart que la prochaine
 * attribution vient combler, sans qu'aucune commande d'administration n'ait a
 * etre lancee.
 */
public class AutoBalance {

    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    public AutoBalance(JavaPlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    /** L'attribution equilibree est-elle active et correctement configuree ? */
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("Auto_Balance.enabled", false)
                && getClans().size() >= 2;
    }

    public List<String> getClans() {
        return plugin.getConfig().getStringList("Auto_Balance.clans");
    }

    /**
     * Rend le clan le moins peuple parmi ceux declares, ou null si aucun n'est
     * configure. Les clans absents de clans.yml comptent zero membre : les
     * ignorer ferait basculer tout le monde du meme cote le jour ou l'un des
     * deux camps n'existe pas encore.
     */
    public String pickClan() {
        List<String> clans = getClans();
        if (clans.isEmpty()) return null;

        MemberCount memberCount = new MemberCount(plugin);
        String best = null;
        int bestCount = Integer.MAX_VALUE;

        for (String clanName : clans) {
            int count = memberCount.getClanMembersCount(clanName);
            if (count < bestCount) {
                bestCount = count;
                best = clanName;
            }
        }
        return best;
    }

    /**
     * Attribue un camp au joueur et l'y ajoute immediatement.
     *
     * @param player  le joueur a placer, qui doit etre en ligne
     * @param notify  destinataire des messages d'erreur ; peut etre le joueur
     *                lui-meme ou un administrateur, et peut etre null quand
     *                l'appel vient de la console
     * @return true si le joueur a bien ete place
     */
    public boolean assign(Player player, org.bukkit.command.CommandSender notify) {
        SearchPlayer searchPlayer = new SearchPlayer(plugin);
        if (searchPlayer.isPlayerInClan(player)) {
            if (notify != null) {
                notify.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
                        + plugin.getConfig().getString("Plugin_Message_Indicator", "| ")
                        + ChatColor.RED + languageManager.get("join.already-member"));
            }
            return false;
        }

        String clanName = pickClan();
        if (clanName == null) {
            if (notify != null) {
                notify.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
                        + plugin.getConfig().getString("Plugin_Message_Indicator", "| ")
                        + ChatColor.RED + languageManager.get("autobalance.not-configured"));
            }
            return false;
        }

        if (!plugin.getConfig().contains("clans." + clanName)
                && !clanExists(clanName)) {
            if (notify != null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("clan_name", clanName);
                notify.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
                        + plugin.getConfig().getString("Plugin_Message_Indicator", "| ")
                        + ChatColor.RED + languageManager.get("autobalance.missing-clan", placeholders));
            }
            return false;
        }

        // PlayerAdder ecrit dans clans.yml, controle max_members et previent le
        // joueur lui-meme. Rien de tout cela n'est reecrit ici.
        AddPlayer addPlayer = new AddPlayer(plugin, languageManager);
        addPlayer.PlayerAdder(clanName, player);
        return true;
    }

    private boolean clanExists(String clanName) {
        java.io.File clansFile = new java.io.File(plugin.getDataFolder(), "clans.yml");
        org.bukkit.configuration.file.FileConfiguration clansConfig =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(clansFile);
        return clansConfig.contains("clans." + clanName);
    }
}
