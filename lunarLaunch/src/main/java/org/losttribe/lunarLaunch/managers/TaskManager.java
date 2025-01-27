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

    private LocationData currentBlock;
    private Player assignedPlayer;
    private BukkitTask timeoutTask;
    private StageManager stageManagerRef;

    public TaskManager(LunarLaunch plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

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

    public void startRandomTask(int timeLimit, StageManager stageManager) {
        this.stageManagerRef = stageManager; // Store reference

        List<String> blocks = plugin.getConfig().getStringList("tasks.blocks");
        if (blocks.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No blocks in config! Use /launch setup first.");
            failTask();
            return;
        }

        String chosen = blocks.get(new Random().nextInt(blocks.size()));
        LocationData locData = stringToLocationData(chosen);
        if (locData == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "Invalid block location: " + chosen);
            failTask();
            return;
        }
        currentBlock = locData;

        Player[] online = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        if (online.length == 0) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players online to do the task!");
            failTask();
            return;
        }
        assignedPlayer = online[new Random().nextInt(online.length)];

        setLampLit(true);

        Bukkit.broadcastMessage(ChatColor.GOLD + "[TASK] " + assignedPlayer.getName()
                + ", interact with the block at " + currentBlock
                + " within " + timeLimit + " seconds!");

        timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "Task failed! Time ran out.");
            failTask();
            cleanup();
        }, timeLimit * 20L);
    }

    public boolean isActiveLamp(Block block) {
        if (currentBlock == null) return false;

        World w = Bukkit.getWorld(currentBlock.worldName);
        if (w == null) return false;

        Block ourLamp = w.getBlockAt(currentBlock.x, currentBlock.y - 1, currentBlock.z);
        return ourLamp.equals(block);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (currentBlock == null || assignedPlayer == null) return;
        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();
        Action action = event.getAction();

        if (action == Action.PHYSICAL) {
            if (!event.getPlayer().equals(assignedPlayer)) return;
            if (!sameBlock(clicked)) return;
            taskSuccess();
        }
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
        }
        cleanup();
    }

    private void failTask() {
        if (stageManagerRef != null) {
            stageManagerRef.onTaskFailure();
        }
    }

    private void cleanup() {
        setLampLit(false);

        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
        currentBlock = null;
        assignedPlayer = null;
        stageManagerRef = null;
    }

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
        return block.getWorld().getName().equals(currentBlock.worldName)
                && block.getX() == currentBlock.x
                && block.getY() == currentBlock.y
                && block.getZ() == currentBlock.z;
    }

    private LocationData stringToLocationData(String s) {
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