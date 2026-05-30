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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardReset extends JavaPlugin implements Listener, CommandExecutor {

    // Délai en ticks avant de reset le scoreboard (20 ticks = 1 seconde)
    // Assez long pour laisser TAB envoyer son reset, mais avant que TheLab envoie son scoreboard
    private static final long RESET_DELAY_TICKS = 5L;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("sbreset").setExecutor(this);
        getLogger().info("ScoreboardReset actif - délai: " + RESET_DELAY_TICKS + " ticks");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // On schedule le reset après le délai configuré
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                resetSidebar(player);
            }
        }, RESET_DELAY_TICKS);
    }

    /**
     * Supprime l'objectif affiché dans le slot SIDEBAR du joueur.
     * Cela force le plugin du mini-jeu à re-créer et renvoyer son scoreboard complet.
     */
    private void resetSidebar(Player player) {
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        }

        // Supprime l'objectif du slot sidebar s'il y en a un
        var objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            objective.unregister();
            getLogger().fine("Sidebar reset pour " + player.getName());
        }

        // Assigne le scoreboard principal propre au joueur
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
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
