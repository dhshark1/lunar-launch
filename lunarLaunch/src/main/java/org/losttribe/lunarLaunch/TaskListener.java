package org.losttribe.lunarLaunch;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public class TaskListener implements Listener {

    private final MaintenanceManager maintenanceManager;

    public TaskListener(MaintenanceManager maintenanceManager) {
        this.maintenanceManager = maintenanceManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            Material type = block.getType();

            if (type.toString().contains("BUTTON")) {
                maintenanceManager.onPlayerAction(MaintenanceTask.TaskAction.PRESS_BUTTON);
            }
            else if (type == Material.LEVER) {
                maintenanceManager.onPlayerAction(MaintenanceTask.TaskAction.FLICK_LEVER);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material placed = event.getBlock().getType();

        if (placed == Material.REDSTONE_WIRE) {
            maintenanceManager.onPlayerAction(MaintenanceTask.TaskAction.PLACE_REDSTONE);
        }
        else if (placed == Material.IRON_BLOCK) {
            maintenanceManager.onPlayerAction(MaintenanceTask.TaskAction.PLACE_IRON_BLOCK);
        }
    }
}
