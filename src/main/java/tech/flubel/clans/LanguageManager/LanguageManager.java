package tech.flubel.clans.LanguageManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Charge les messages du fichier de langue et les MET EN FORME.
 *
 * <p>La mise en forme est l'ajout du fork. Le plugin d'origine rendait la
 * chaine brute, et chaque appelant la prefixait d'un {@code ChatColor} ecrit en
 * dur - rouge pour les erreurs, vert pour les succes, or pour les usages. Un
 * serveur ayant sa propre palette ne pouvait donc rien changer sans repasser
 * sur la trentaine de sites d'appel.
 *
 * <p>Les messages acceptent maintenant les codes {@code &} et les couleurs
 * hexadecimales {@code &#RRGGBB}. Comme une couleur posee dans le message
 * arrive APRES le ChatColor ecrit en dur, c'est elle qui l'emporte - et comme
 * un code de couleur remet le gras a zero, le {@code ChatColor.BOLD} du prefixe
 * tombe avec. Un message qui commence par sa propre couleur reprend donc
 * entierement la main sur son apparence, sans qu'aucun site d'appel n'ait ete
 * touche.
 *
 * <p>Mettre {@code Plugin_Message_Indicator} a la chaine vide dans config.yml
 * ne laisse alors plus rien du formatage d'origine.
 */
public class LanguageManager {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private YamlConfiguration lang;

    public LanguageManager(JavaPlugin plugin) {
        String selectedLang = plugin.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "lang/" + selectedLang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file not found: " + selectedLang + ".yml");
            // fallback to English or another default
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
        }
        this.lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public String get(String path) {
        return colorize(lang.getString(path, "Message not found: " + path));
    }

    public String get(String path, Map<String, String> placeholders) {
        String message = lang.getString(path, "Message not found: " + path);
        if (message == null) return "Message not found: " + path;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return colorize(message);
    }

    /**
     * Traduit les couleurs hexadecimales puis les codes {@code &} classiques.
     *
     * <p>L'ordre compte : {@code &#RRGGBB} contient un {@code &} que la seconde
     * passe consommerait autrement, laissant un {@code #RRGGBB} en clair.
     */
    public static String colorize(String message) {
        if (message == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(
                            net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString()));
        }
        matcher.appendTail(buffer);

        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
