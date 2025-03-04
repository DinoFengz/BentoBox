package world.bentobox.bentobox.listeners.flags.worldsettings;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import world.bentobox.bentobox.api.flags.FlagListener;
import world.bentobox.bentobox.lists.Flags;

/**
 * Removes mobs when teleporting to an island
 * @author tastybento
 *
 */
public class RemoveMobsListener extends FlagListener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onUserTeleport(PlayerTeleportEvent e) {
        // Return if this isn't a genuine teleport
        if (e.getCause().equals(TeleportCause.CHORUS_FRUIT) || e.getCause().equals(TeleportCause.ENDER_PEARL)
                || e.getCause().equals(TeleportCause.SPECTATE)) {
            return;
        }
        if(e.getTo() == null)
            return;

        
        // Return if this is a small teleport
        if (e.getTo().getWorld().equals(e.getPlayer().getWorld()) &&
                e.getTo().distanceSquared(e.getPlayer().getLocation()) < getPlugin().getSettings().getClearRadius() * getPlugin().getSettings().getClearRadius()) {
            return;
        }
        // Only process if flag is active
        if (getIslands().locationIsOnIsland(e.getPlayer(), e.getTo()) && Flags.REMOVE_MOBS.isSetForWorld(e.getTo().getWorld())) {
            Bukkit.getScheduler().runTask(getPlugin(), () -> getIslands().clearArea(e.getTo()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onUserRespawn(PlayerRespawnEvent e) {
        // Only process if flag is active
        if (getIslands().locationIsOnIsland(e.getPlayer(), e.getRespawnLocation()) && Flags.REMOVE_MOBS.isSetForWorld(e.getRespawnLocation().getWorld())) {
            Bukkit.getScheduler().runTask(getPlugin(), () -> getIslands().clearArea(e.getRespawnLocation()));
        }
    }

}
