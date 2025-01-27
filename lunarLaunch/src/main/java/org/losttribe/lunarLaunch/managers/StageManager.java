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
    private int tasksCompletedInStage;

    private static final int TASKS_PER_STAGE = 3;

    public StageManager(LunarLaunch plugin, TaskManager taskManager) {
        this.plugin = plugin;
        this.taskManager = taskManager;
    }

    public void startLaunchFlow() {
        running = true;
        startStage(LaunchStage.LIFTOFF);
    }

    private void startStage(LaunchStage stage) {
        this.currentStage = stage;
        this.tasksCompletedInStage = 0;

        broadcast(ChatColor.YELLOW + "===== " + stage.getDisplayName() + " STAGE =====");
        broadcast(ChatColor.GRAY + stage.getDescription());

        startNextTaskInStage();
    }

    public void startNextTaskInStage() {
        if (tasksCompletedInStage >= TASKS_PER_STAGE) {
            goToNextStage();
            return;
        }

        int nextTaskIndex = tasksCompletedInStage + 1;
        broadcast(ChatColor.AQUA + "[INFO] We will start task #" + nextTaskIndex
                + " for " + currentStage.name() + " in 5 seconds...");

        taskManager.startPreTaskCountdown(5, () -> {
            int timeLimit;
            switch (currentStage) {
                case LIFTOFF: timeLimit = 7; break;
                case CRUISE:  timeLimit = 5; break;
                case LANDING: timeLimit = 3; break;
                default:      timeLimit = 5;
            }
            broadcast(ChatColor.AQUA + "[INFO] Starting task #" + nextTaskIndex
                    + " (Time limit: " + timeLimit + "s)...");
            taskManager.startRandomTask(timeLimit, this);
        });
    }

    private void goToNextStage() {
        broadcast(ChatColor.GREEN + "Completed all " + TASKS_PER_STAGE
                + " tasks in " + currentStage.getDisplayName() + " stage!");
        if (currentStage == LaunchStage.LIFTOFF) {
            startStage(LaunchStage.CRUISE);
        } else if (currentStage == LaunchStage.CRUISE) {
            startStage(LaunchStage.LANDING);
        } else {
            broadcast(ChatColor.GREEN + "Congratulations! You've landed successfully!");
            running = false;
        }
    }

    public void onTaskSuccess() {
        tasksCompletedInStage++;
        broadcast(ChatColor.YELLOW + "Task #" + tasksCompletedInStage + " in "
                + currentStage.name() + " was completed successfully!");

        startNextTaskInStage();
    }

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