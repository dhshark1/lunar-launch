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

import org.losttribe.lunarLaunch.models.LaunchStage;

import java.util.List;
import java.util.Random;

public class TaskManager implements Listener {

    private final LunarLaunch plugin;

    private Location currentBlock;
    private Player assignedPlayer;
    private BukkitTask timeoutTask;
    private StageManager stageManagerRef;

    public TaskManager(LunarLaunch plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Start the countdown before a task
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
     * Pick a random block from config, pick a random player, start the time-limited task
     */
    public void startRandomTask(int timeLimit, StageManager stageManager) {
        this.stageManagerRef = stageManager;

        List<String> blocks = plugin.getConfig().getStringList("tasks.blocks");
        if (blocks.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No blocks in config! Use /launch setup first.");
            stageManagerRef.onTaskFailure();
            return;
        }

        // pick random block from the list
        String chosen = blocks.get(new Random().nextInt(blocks.size()));
        currentBlock = stringToLocation(chosen);
        if (currentBlock == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "Invalid block location: " + chosen);
            stageManagerRef.onTaskFailure();
            return;
        }

        // pick random player
        Player[] online = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        if (online.length == 0) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players online to complete the task!");
            stageManagerRef.onTaskFailure();
            return;
        }
        assignedPlayer = online[new Random().nextInt(online.length)];

        // light the lamp beneath
        setLampLit(true);

        Bukkit.broadcastMessage(ChatColor.GOLD + "[TASK] " + assignedPlayer.getName()
                + ", interact with the block at " + currentBlockToString()
                + " within " + timeLimit + " seconds!");

        // set the timeout
        timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "Task failed! Time ran out.");
            cleanup();
            stageManagerRef.onTaskFailure();
        }, timeLimit * 20L);
    }

    /**
     * Listen for lever/button/plate interaction
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (currentBlock == null || assignedPlayer == null) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Action action = event.getAction();

        // pressure plate => PHYSICAL, lever/button => RIGHT_CLICK_BLOCK
        if (action == Action.PHYSICAL) {
            if (!event.getPlayer().equals(assignedPlayer)) return;
            if (!clickedBlock.getLocation().equals(currentBlock)) return;
            // success
            taskSuccess();
        }
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            if (!event.getPlayer().equals(assignedPlayer)) return;
            if (!clickedBlock.getLocation().equals(currentBlock)) return;
            // success
            taskSuccess();
        }
    }

    private void taskSuccess() {
        Bukkit.broadcastMessage(ChatColor.GREEN + assignedPlayer.getName() + " completed the task successfully!");
        cleanup();
        stageManagerRef.onTaskSuccess();
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

    /**
     * Light/unlight the redstone lamp below currentBlock
     */
    private void setLampLit(boolean lit) {
        if (currentBlock == null) return;
        Location below = currentBlock.clone().add(0, -1, 0);
        if (below.getBlock().getType() == Material.REDSTONE_LAMP) {
            if (below.getBlock().getBlockData() instanceof Lightable) {
                Lightable lampData = (Lightable) below.getBlock().getBlockData();
                lampData.setLit(lit);
                below.getBlock().setBlockData(lampData);
            }
        }
    }

    private String currentBlockToString() {
        return "(" + currentBlock.getWorld().getName() + " "
                + currentBlock.getBlockX() + ","
                + currentBlock.getBlockY() + ","
                + currentBlock.getBlockZ() + ")";
    }

    private Location stringToLocation(String s) {
        // "world,x,y,z"
        String[] parts = s.split(",");
        if (parts.length != 4) return null;
        World w = Bukkit.getWorld(parts[0]);
        if (w == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(w, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
