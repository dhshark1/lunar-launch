package org.losttribe.lunarLaunch.listeners;

import org.losttribe.lunarLaunch.LunarLaunch;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class SetupListener implements Listener {

    private final LunarLaunch plugin;

    public SetupListener(LunarLaunch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isSetupMode()) return;
        if (plugin.getStageManager().isRunning()) {
            return;
        }

        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();
        Material type = clicked.getType();

        if (isInteractiveBlock(type)) {
            Block below = clicked.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.REDSTONE_LAMP) {
                List<String> blockList = plugin.getConfig().getStringList("tasks.blocks");
                if (blockList == null) {
                    blockList = new ArrayList<>();
                }

                String locStr = locationToString(clicked);

                if (blockList.contains(locStr)) {
                    event.getPlayer().sendMessage(ChatColor.RED
                            + "This block is already recorded in config.yml!");
                } else {
                    blockList.add(locStr);
                    plugin.getConfig().set("tasks.blocks", blockList);
                    plugin.saveConfig();

                    event.getPlayer().sendMessage(ChatColor.GREEN
                            + "Recorded " + type + " at " + locStr + " (on a Redstone Lamp).");
                }
            } else {
                event.getPlayer().sendMessage(ChatColor.RED
                        + "That block is NOT on top of a redstone lamp!");
            }
            event.setCancelled(true);
        }
    }

    private boolean isInteractiveBlock(Material m) {
        switch (m) {
            case LEVER:
            case STONE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case BIRCH_BUTTON:
            case JUNGLE_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case CRIMSON_BUTTON:
            case WARPED_BUTTON:
            case STONE_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case CRIMSON_PRESSURE_PLATE:
            case WARPED_PRESSURE_PLATE:
                return true;
            default:
                return false;
        }
    }

    private String locationToString(Block block) {
        return block.getWorld().getName() + ","
                + block.getX() + ","
                + block.getY() + ","
                + block.getZ();
    }
}