package org.losttribe.lunarLaunch.managers;

import org.losttribe.lunarLaunch.LunarLaunch;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class TaskManager implements Listener {

    private final LunarLaunch plugin;

    private LocationData currentBlock;  // The block that must be interacted with
    private Player assignedPlayer;      // The player chosen for this task
    private BukkitTask timeoutTask;
    private StageManager stageManagerRef;

    public TaskManager(LunarLaunch plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 5-second countdown before launching the next task
     */
    public void startPreTaskCountdown(int seconds, Runnable taskStarter) {
        new BukkitRunnable() {
            int countdown = seconds;
            @Override
            public void run() {
                if (countdown <= 0) {
                    cancel();
                    taskStarter.run();
                    return;
                }
                Bukkit.broadcastMessage(ChatColor.AQUA + "Next task starts in " + countdown + "...");
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Start a single random task: pick random block from config,
     * pick random player, set a time limit, light the lamp
     */
    public void startRandomTask(int timeLimit, StageManager stageManager) {
        this.stageManagerRef = stageManager; // Store reference

        List<String> blocks = plugin.getConfig().getStringList("tasks.blocks");
        if (blocks.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No blocks in config! Use /launch setup first.");
            failTask();
            return;
        }

        // Pick random block
        String chosen = blocks.get(new Random().nextInt(blocks.size()));
        LocationData locData = stringToLocationData(chosen);
        if (locData == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "Invalid block location: " + chosen);
            failTask();
            return;
        }
        currentBlock = locData;

        // Pick random player
        Player[] online = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        if (online.length == 0) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players online to do the task!");
            failTask();
            return;
        }
        assignedPlayer = online[new Random().nextInt(online.length)];

        // Light the lamp beneath the chosen block
        setLampLit(true);

        Bukkit.broadcastMessage(ChatColor.GOLD + "[TASK] " + assignedPlayer.getName()
                + ", interact with the block at " + currentBlock
                + " within " + timeLimit + " seconds!");

        // Timeout
        timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "Task failed! Time ran out.");
            failTask();
            cleanup();
        }, timeLimit * 20L);
    }

    public boolean isActiveLamp(Block block) {
        if (currentBlock == null) return false;

        // The lamp is presumably at y = currentBlock.y - 1, same x,z
        World w = Bukkit.getWorld(currentBlock.worldName);
        if (w == null) return false;

        Block ourLamp = w.getBlockAt(currentBlock.x, currentBlock.y - 1, currentBlock.z);
        return ourLamp.equals(block);
    }

    /**
     * Check the player's interaction
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (currentBlock == null || assignedPlayer == null) return;
        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();
        Action action = event.getAction();

        // For a pressure plate => PHYSICAL
        if (action == Action.PHYSICAL) {
            if (!event.getPlayer().equals(assignedPlayer)) return;
            if (!sameBlock(clicked)) return;
            taskSuccess();
        }
        // For button/lever => RIGHT_CLICK_BLOCK
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            if (!event.getPlayer().equals(assignedPlayer)) return;
            if (!sameBlock(clicked)) return;
            taskSuccess();
        }
    }

    private void taskSuccess() {
        Bukkit.broadcastMessage(ChatColor.GREEN
                + assignedPlayer.getName() + " completed the task successfully!");

        if (stageManagerRef != null) {
            stageManagerRef.onTaskSuccess();
        } else {
            plugin.getLogger().warning("stageManagerRef is null on success?!");
        }
        cleanup();
    }

    private void failTask() {
        if (stageManagerRef != null) {
            stageManagerRef.onTaskFailure();
        }
    }

    private void cleanup() {
        // Turn off the lamp
        setLampLit(false);

        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
        currentBlock = null;
        assignedPlayer = null;
        stageManagerRef = null;
    }

    /**
     * Light or unlight the lamp below currentBlock
     */
    private void setLampLit(boolean lit) {
        if (currentBlock == null) return;
        LocationData below = new LocationData(currentBlock.worldName, currentBlock.x, currentBlock.y - 1, currentBlock.z);
        Block lamp = below.getBlock();
        if (lamp.getType() == Material.REDSTONE_LAMP && lamp.getBlockData() instanceof Lightable) {
            Lightable lampData = (Lightable) lamp.getBlockData();
            lampData.setLit(lit);
            lamp.setBlockData(lampData);
        }
    }

    private boolean sameBlock(Block block) {
        // compare coordinates
        return block.getWorld().getName().equals(currentBlock.worldName)
                && block.getX() == currentBlock.x
                && block.getY() == currentBlock.y
                && block.getZ() == currentBlock.z;
    }

    private LocationData stringToLocationData(String s) {
        // "world,x,y,z"
        String[] parts = s.split(",");
        if (parts.length != 4) return null;
        try {
            String w = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new LocationData(w, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * A small container for block coords
     */
    private static class LocationData {
        String worldName;
        int x, y, z;

        LocationData(String worldName, int x, int y, int z) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Block getBlock() {
            World w = Bukkit.getWorld(worldName);
            if (w == null) return null;
            return w.getBlockAt(x, y, z);
        }

        @Override
        public String toString() {
            return "(" + worldName + " " + x + "," + y + "," + z + ")";
        }
    }
}