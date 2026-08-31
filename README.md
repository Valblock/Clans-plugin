
![sssssssssssss_(18)-transformed](https://github.com/user-attachments/assets/a65852f8-a1e8-4612-948e-e545491fc643)

![](https://img.shields.io/github/actions/workflow/status/Flubel/Clans/maven.yml?branch=master&style=for-the-badge&color=ff5555)
![](https://img.shields.io/modrinth/dt/simple-clans?style=for-the-badge&logo=modrinth&label=Modrinth%20Downloads&color=2ECC71)
![](https://img.shields.io/spiget/downloads/123903?style=for-the-badge&logo=SpigotMC&color=E67E22&label=Spigot%20Downloads)
![](https://img.shields.io/hangar/dt/Clans?style=for-the-badge&logo=Hangar&color=3498DB&label=Hangar%20Downloads)
![](https://img.shields.io/github/downloads/Flubel/Clans/total?style=for-the-badge&logo=Github&color=95A5A6&label=Github%20Downloads)
![](https://img.shields.io/bstats/players/25416?style=for-the-badge&logo=datadog&color=9B59B6&label=Active%20Players)
![](https://img.shields.io/bstats/servers/25416?style=for-the-badge&logo=serverfault&color=F1C40F&label=Active%20Servers)


The Clans Plugin is the ultimate tool for fostering teamwork and competition on your Minecraft server. With a rich set of features, players can create, manage, and grow their clans while competing for dominance. Whether you're building a community or running a competitive server, this plugin is designed to enhance the multiplayer experience.

## Features
1. **Clan Creation**: Players can create clans with unique names and prefixes.
2. **Clan Management**:
   - Invite, promote, demote, or kick members.
   - Transfer leadership to other members.
3. **Clan Chat**: Communicate privately with your clan using /cc or /clan chat.
4. **Clan Chest**: A virtual clan Chest shared amongst clan members that can also be upgraded (9-54).
5. **Clan Home**:
   - Set a home location for your clan.
   - Teleport to the clan home anytime.
6. **Clan Banner**: Set a clan Banner to assert Land Dominance.
7. **Pvp Management**: Leaders can now enable or disable pvp amongst clan members (own and allied clans).
8. **Clan Enemies/Allies**: Clans can now declare Enemies and Alliance.
9. **Clan Upgrades**: Expand your clan's player slots with upgrades.
10. **Join Requests**:
   - Send requests to join clans.
   - Accept or deny join requests as a leader.
   - Toggle Clan Join status.
11. **Economy Integration**: Requires Vault to manage clan creation costs and upgrades.
12. **PlaceholderAPI Support**: Display clan-related placeholders anywhere on your server.
13. **Highly Configurable**: Customize settings like clan creation costs, max members, and more.
14. **Multi-Language Support**: Plugin supports up to 6 languages that are en, fr, dt, ru, tr, cn.
15. **Hex Colored Clan Names**: Clan names now support custom 6-digit hex color codes (e.g. #178ad6).

## Placeholders
These placeholders are available when using [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/):

- `%clans_name%` → Shows the player's clan name
- `%clans_name_cm%` → Shows the player's clan name (for ChatManager Only)
- `%clans_badge%` → Shows the player's clan badge
- `%clans_list_<#>_name%` → Shows the name of the clan at that position in the leaderboard
- `%clans_list_<#>_leader%` → Shows the leader of the clan at that position
- `%clans_list_<#>_balance%` → Shows the balance of the clan at that position
- `%clans_list_<#>_kdr%` → Shows the KDR of the clan at that position


## Installation
1. Download the latest release from:
   - [SpigotMC](https://www.spigotmc.org/resources/clans.123903/)
   - [Modrinth](https://modrinth.com/plugin/simple-clans)
   - [Hangar](https://hangar.papermc.io/MrFiend/Clans)
2. Place the `.jar` file into your server’s `plugins/` folder.
3. Restart your server.

## Notice
The plugin functions fully without **[Fcore](https://modrinth.com/plugin/fcore)**, with one exception: **Offline Player Kicking** requires Fcore, as it is responsible for tracking offline player stats.

## Documentation
Full setup guides and advanced usage can be found in the [Wiki](../../wiki).

## Statistics
The plugin stats can be found on [Bstats](https://bstats.org/plugin/bukkit/Simple%20Clans/25416)

![](https://bstats.org/signatures/bukkit/Simple%20Clans.svg)
---

## Fork ValBlock

Ce fork ajoute ce qu'il faut pour des **clans appartenant au serveur** plutôt
qu'à des joueurs : des camps fixes, sans chef humain pour valider les
adhésions.

### Le problème du plugin d'origine

`/clan join <nom>` dépose une **demande** qu'un chef doit accepter, et
`onCommand` refuse la console dès sa première ligne. Sur un serveur où les
clans sont des camps appartenant au serveur, il n'y a personne pour accepter :
les demandes s'empilent indéfiniment, et aucun automate ne peut placer un
joueur à la place du chef.

### `Auto_Balance` — attribution automatique et équilibrée

```yaml
Auto_Balance:
  enabled: true
  clans:
    - Kattegat
    - Finehair
```

Quand c'est actif, **`/clan join` ignore l'argument de clan** et attribue au
joueur le camp le **moins peuplé** de la liste, sans demande ni validation.
L'écart entre les camps ne dépasse donc jamais un membre.

Le comptage se fait sur l'état réel de `clans.yml` **à chaque attribution**,
jamais sur un compteur tenu à part. C'est ce qui rend l'équilibre
auto-correctif : un départ par `/clan leave` ou `/clan kick` creuse un écart
que l'attribution suivante vient combler toute seule, sans qu'aucune commande
d'administration n'ait à être lancée.

En cas d'égalité, le premier de la liste l'emporte — déterministe, donc
testable. Il faut au moins deux clans pour que le mode s'active, et ils doivent
exister (`/clan create`) avant d'être listés.

### `/clanadmin` — la porte d'entrée console

`/clan` garde son garde-fou « joueur uniquement » intact : la surface
d'administration est une commande **séparée**, protégée par `clans.admin`.

| Commande | Effet |
|---|---|
| `/clanadmin autojoin <joueur>` | attribution équilibrée, exécutable depuis la console |
| `/clanadmin add <joueur> <clan>` | ajout forcé, sans passer par une demande |
| `/clanadmin remove <joueur>` | retrait forcé, y compris hors ligne — refuse un chef |
| `/clanadmin balance` | diagnostic : membres par camp, et le prochain camp attribué |

`autojoin` et `add` exigent que le joueur soit **en ligne** : `AddPlayer` lit
ses statistiques de kills et de morts pour les ajouter au clan.

### Correctif : `SearchPlayer` levait une `NullPointerException`

`co_leader` est une **liste**, et elle était lue avec `getString`, qui rend
`null` — le `.equals` qui suivait explosait. Le bug restait invisible tant que
`clans.yml` était vide, puisque la boucle ne s'exécutait jamais ; il se
déclenchait dès le **premier clan créé**, cassant `/clan create` pour tout le
monde. La section `clans` absente n'était pas gardée non plus.
