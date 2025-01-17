package org.losttribe.lunarLaunch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceManager {

    // Each maintenance task must be completed within this time in seconds
    private static final int TASK_COMPLETION_TIME_SECONDS = 20;

    // Currently active tasks
    private final List<MaintenanceTask> activeTasks = new ArrayList<>();

    /**
     * Spawns a single "multi-step" MaintenanceTask.
     */
    public void spawnRandomMaintenanceTask() {
        MaintenanceTask task = new MaintenanceTask(TASK_COMPLETION_TIME_SECONDS);
        activeTasks.add(task);
        task.start();
    }

    /**
     * Called by TaskListener whenever a player triggers an interaction or places a block.
     * We forward this action to each task to see if it matches their next required step.
     */
    public void onPlayerAction(MaintenanceTask.TaskAction action) {
        for (MaintenanceTask task : new ArrayList<>(activeTasks)) {
            if (!task.isCompleted()) {
                task.progressTask(action);
                if (task.isCompleted()) {
                    completeTask(task);
                }
            }
        }
    }

    /**
     * A task timed out or otherwise failed
     */
    public void failTask(MaintenanceTask task) {
        // Remove this task from active tasks
        activeTasks.remove(task);

        // Immediately fail the entire launch
        LunarLaunch.getInstance().getLaunchManager().failLaunch();
    }

    /**
     * A task is completed
     */
    private void completeTask(MaintenanceTask task) {
        broadcast(ChatColor.GREEN + "Maintenance Task COMPLETED: " + task.getName());
        activeTasks.remove(task);
    }

    /**
     * Called to stop all tasks (on plugin disable or launch reset)
     */
    public void stopAllTasks() {
        for (MaintenanceTask task : new ArrayList<>(activeTasks)) {
            task.cancelTimeout();
        }
        activeTasks.clear();
    }

    private void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}

