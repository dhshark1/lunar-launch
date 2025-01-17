package org.losttribe.lunarLaunch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class LaunchManager {

    private static final int LAUNCH_DURATION_SECONDS = 300;  // 5 minutes
    private static final int MIN_TASK_INTERVAL_SECONDS = 15;
    private static final int MAX_TASK_INTERVAL_SECONDS = 45;

    private int launchTimer = LAUNCH_DURATION_SECONDS;
    private boolean isLaunching = false;

    private BukkitTask launchCountdownTask;
    private BukkitTask maintenanceSpawnTask;

    private final MaintenanceManager maintenanceManager;

    public LaunchManager(MaintenanceManager maintenanceManager) {
        this.maintenanceManager = maintenanceManager;
    }

    public void startLaunchSequence() {
        if (isLaunching) {
            return;
        }
        isLaunching = true;
        launchTimer = LAUNCH_DURATION_SECONDS;

        broadcast(ChatColor.GREEN + "Rocket launch sequence initiated!");
        broadcast(ChatColor.YELLOW + "You have 5 minutes until lift-off. Complete maintenance tasks!");

        launchCountdownTask = Bukkit.getScheduler().runTaskTimer(
                LunarLaunch.getInstance(),
                () -> {
                    launchTimer--;
                    if (launchTimer % 10 == 0 || launchTimer <= 10) {
                        broadcast(ChatColor.AQUA + "Launch in " + launchTimer + " seconds...");
                    }
                    if (launchTimer <= 0) {
                        completeLaunchSuccessfully();
                    }
                },
                20L,
                20L
        );

        scheduleNextMaintenanceTask();
    }

    /**
     * Helper to let the command (or others) check if a launch is active.
     */
    public boolean isLaunching() {
        return isLaunching;
    }

    private void scheduleNextMaintenanceTask() {
        if (!isLaunching) return;

        int interval = ThreadLocalRandom.current().nextInt(MIN_TASK_INTERVAL_SECONDS, MAX_TASK_INTERVAL_SECONDS + 1);
        maintenanceSpawnTask = Bukkit.getScheduler().runTaskLater(
                LunarLaunch.getInstance(),
                () -> {
                    maintenanceManager.spawnRandomMaintenanceTask();
                    scheduleNextMaintenanceTask();
                },
                interval * 20L
        );
    }

    private void completeLaunchSuccessfully() {
        broadcast(ChatColor.GREEN + "All maintenance tasks completed successfully!");
        broadcast(ChatColor.GREEN + "Rocket launch successful! Congratulations!");
        resetLaunch();
    }

    public void failLaunch() {
        broadcast(ChatColor.DARK_RED + "MAINTENANCE FAILURE! Launch sequence aborted.");
        resetLaunch();
    }

    private void resetLaunch() {
        stopAllTasks();
        maintenanceManager.stopAllTasks();
    }

    public void stopAllTasks() {
        if (launchCountdownTask != null) {
            launchCountdownTask.cancel();
            launchCountdownTask = null;
        }
        if (maintenanceSpawnTask != null) {
            maintenanceSpawnTask.cancel();
            maintenanceSpawnTask = null;
        }
        isLaunching = false;
    }

    private void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}
