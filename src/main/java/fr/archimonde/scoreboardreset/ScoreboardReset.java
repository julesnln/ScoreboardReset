package fr.archimonde.scoreboardreset;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardReset extends JavaPlugin implements Listener, CommandExecutor {

    private static final long RESET_DELAY_TICKS = 0L;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("sbreset").setExecutor(this);
        getLogger().info("ScoreboardReset actif - délai: " + RESET_DELAY_TICKS + " ticks");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                resetSidebar(player);
            }
        }, RESET_DELAY_TICKS);
    }

    private void resetSidebar(Player player) {
        // Assigner un scoreboard tout neuf et vide au joueur
        // Sans unregister, sans détruire l'objectif existant
        Scoreboard fresh = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(fresh);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }
        if (!player.hasPermission("scoreboardreset.use")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }
        resetSidebar(player);
        player.sendMessage("§aScoreboard sidebar reset !");
        return true;
    }
}
