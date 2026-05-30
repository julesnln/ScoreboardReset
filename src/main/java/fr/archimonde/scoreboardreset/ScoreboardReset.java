package fr.archimonde.scoreboardreset;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardReset extends JavaPlugin {

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Bloque tous les packets scoreboard envoyés AU client
        // PacketType.Play.Server.SCOREBOARD_OBJECTIVE  → création/suppression d'objectif
        // PacketType.Play.Server.SCOREBOARD_SCORE      → mise à jour des lignes
        // PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE → affichage dans un slot

        protocolManager.addPacketListener(new PacketAdapter(
                this,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_SCORE,
                PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Annule le packet → TheLab reçoit rien de TAB/BungeeCord
                // et peut envoyer son propre scoreboard sans interférence
                event.setCancelled(true);
            }
        });

        getLogger().info("ScoreboardReset actif - packets scoreboard bloqués via ProtocolLib");
    }

    @Override
    public void onDisable() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
        }
    }
}
