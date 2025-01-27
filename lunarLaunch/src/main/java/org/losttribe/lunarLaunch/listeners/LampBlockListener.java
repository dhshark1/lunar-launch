package org.losttribe.lunarLaunch.listeners;

import org.losttribe.lunarLaunch.managers.TaskManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * Prevents normal redstone signals from lighting any redstone lamps
 * that aren't the "active" one chosen by TaskManager.
 */
public class LampBlockListener implements Listener {

    private final TaskManager taskManager;

    public LampBlockListener(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();

        // If the block is a redstone lamp
        if (block.getType() == Material.REDSTONE_LAMP) {
            // Check if it's the "active" lamp that TaskManager wants lit
            if (!taskManager.isActiveLamp(block)) {
                // It's not the active lamp => forcibly unlight it
                if (block.getBlockData() instanceof Lightable) {
                    Lightable lampData = (Lightable) block.getBlockData();
                    lampData.setLit(false);
                    block.setBlockData(lampData);
                }

                // Optionally, set the redstone power to 0 so it won't stay powered
                event.setNewCurrent(0);
            }
        }
    }
}
