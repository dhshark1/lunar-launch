package org.losttribe.lunarLaunch.listeners;

import org.losttribe.lunarLaunch.managers.TaskManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class LampBlockListener implements Listener {

    private final TaskManager taskManager;

    public LampBlockListener(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.REDSTONE_LAMP) {
            if (!taskManager.isActiveLamp(block)) {
                if (block.getBlockData() instanceof Lightable) {
                    Lightable lampData = (Lightable) block.getBlockData();
                    lampData.setLit(false);
                    block.setBlockData(lampData);
                }

                event.setNewCurrent(0);
            }
        }
    }
}
