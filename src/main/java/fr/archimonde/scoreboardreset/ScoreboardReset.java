package fr.archimonde.scoreboardreset;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardReset extends JavaPlugin implements Listener {

    // Durée en ticks pendant laquelle on bloque les packets TAB après le join
    // TheLab envoie son scoreboard immédiatement (0ms), TAB a un délai de 1000ms
    // On bloque donc pendant 1500ms (30 ticks) pour être sûr
    private static final long BLOCK_DURATION_TICKS = 30L;

    // Set des joueurs dont on bloque actuellement les packets scoreboard
    private final Set<UUID> blockedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        Bukkit.getPluginManager().registerEvents(this, this);

        // Intercepte les packets scoreboard entrants SEULEMENT pour les joueurs bloqués
        protocolManager.addPacketListener(new PacketAdapter(
                this,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_SCORE,
                PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Bloquer uniquement si le joueur est dans la liste
                if (blockedPlayers.contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        });

        getLogger().info("ScoreboardReset actif - blocage de " + BLOCK_DURATION_TICKS + " ticks après join");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Bloquer immédiatement les packets TAB à l'arrivée
        blockedPlayers.add(uuid);

        // Débloquer après la durée configurée
        // TheLab aura eu le temps d'envoyer son scoreboard, TAB sera bloqué pendant ce temps
        Bukkit.getScheduler().runTaskLater(this, () -> {
            blockedPlayers.remove(uuid);
        }, BLOCK_DURATION_TICKS);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Nettoyer la liste quand le joueur part
        blockedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onDisable() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
        }
        blockedPlayers.clear();
    }
}
