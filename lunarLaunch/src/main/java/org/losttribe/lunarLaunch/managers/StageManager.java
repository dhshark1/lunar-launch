package org.losttribe.lunarLaunch.managers;

import org.losttribe.lunarLaunch.LunarLaunch;
import org.losttribe.lunarLaunch.models.LaunchStage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class StageManager {

    private final LunarLaunch plugin;
    private final TaskManager taskManager;
    private boolean running = false;

    private LaunchStage currentStage;
    private int tasksCompletedInStage; // how many tasks have been done

    // How many tasks you want per stage:
    private static final int TASKS_PER_STAGE = 3;

    public StageManager(LunarLaunch plugin, TaskManager taskManager) {
        this.plugin = plugin;
        this.taskManager = taskManager;
    }

    public void startLaunchFlow() {
        running = true;
        startStage(LaunchStage.LIFTOFF);
    }

    /**
     * Resets the stage counters and announces the new stage.
     */
    private void startStage(LaunchStage stage) {
        this.currentStage = stage;
        this.tasksCompletedInStage = 0;

        broadcast(ChatColor.YELLOW + "===== " + stage.getDisplayName() + " STAGE =====");
        broadcast(ChatColor.GRAY + stage.getDescription());

        // Start the first task for this stage
        startNextTaskInStage();
    }

    /**
     * Checks if we've done enough tasks for this stage (3 by default).
     * If not, we do a 5-second countdown, then start a new task.
     */
    public void startNextTaskInStage() {
        // If we've already done all tasks for the stage, move on
        if (tasksCompletedInStage >= TASKS_PER_STAGE) {
            goToNextStage();
            return;
        }

        // The index of the *next* task is tasksCompletedInStage+1
        int nextTaskIndex = tasksCompletedInStage + 1;
        broadcast(ChatColor.AQUA + "[INFO] We will start task #" + nextTaskIndex
                + " for " + currentStage.name() + " in 5 seconds...");

        // 5-second pre-task countdown
        taskManager.startPreTaskCountdown(5, () -> {
            int timeLimit;
            switch (currentStage) {
                case LIFTOFF: timeLimit = 7; break;
                case CRUISE:  timeLimit = 5; break;
                case LANDING: timeLimit = 3; break;
                default:      timeLimit = 5;
            }
            // Now actually start the random task
            broadcast(ChatColor.AQUA + "[INFO] Starting task #" + nextTaskIndex
                    + " (Time limit: " + timeLimit + "s)...");
            taskManager.startRandomTask(timeLimit, this);
        });
    }

    /**
     * Move to the next stage if the current stage is done.
     */
    private void goToNextStage() {
        broadcast(ChatColor.GREEN + "Completed all " + TASKS_PER_STAGE
                + " tasks in " + currentStage.getDisplayName() + " stage!");
        if (currentStage == LaunchStage.LIFTOFF) {
            startStage(LaunchStage.CRUISE);
        } else if (currentStage == LaunchStage.CRUISE) {
            startStage(LaunchStage.LANDING);
        } else {
            // LANDING done
            broadcast(ChatColor.GREEN + "Congratulations! You've landed successfully!");
            running = false;
        }
    }

    /**
     * Called by TaskManager when a task finishes successfully.
     */
    public void onTaskSuccess() {
        tasksCompletedInStage++;
        broadcast(ChatColor.YELLOW + "Task #" + tasksCompletedInStage + " in "
                + currentStage.name() + " was completed successfully!");
        plugin.getLogger().info("[DEBUG] tasksCompletedInStage = " + tasksCompletedInStage);

        // Start the next one (or move on if we've done them all)
        startNextTaskInStage();
    }

    /**
     * Called by TaskManager when a task fails or times out.
     * Restart the entire stage from task #1 (or do a 10s countdown if desired).
     */
    public void onTaskFailure() {
        broadcast(ChatColor.RED + "A task failed! Restarting the " + currentStage.name() + " stage from the beginning.");
        startStage(currentStage);
    }

    public boolean isRunning() {
        return running;
    }

    private void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}