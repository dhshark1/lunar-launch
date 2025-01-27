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

    private void startNextTaskInStage() {
        // If we've completed 3 tasks in this stage, move on
        if (tasksCompletedInStage >= 3) {
            goToNextStage();
            return;
        }

        // 5-second pre-task countdown
        taskManager.startPreTaskCountdown(5, () -> {
            // Start an actual task
            int timeLimit;
            switch (currentStage) {
                case LIFTOFF: timeLimit = 7; break;
                case CRUISE:  timeLimit = 5; break;
                case LANDING: timeLimit = 3; break;
                default:      timeLimit = 5;
            }
            taskManager.startRandomTask(timeLimit, this);
        });
    }

    private void goToNextStage() {
        if (currentStage == LaunchStage.LIFTOFF) {
            startStage(LaunchStage.CRUISE);
        } else if (currentStage == LaunchStage.CRUISE) {
            startStage(LaunchStage.LANDING);
        } else {
            // Completed LANDING
            broadcast(ChatColor.GREEN + "Congratulations! You've landed successfully!");
            running = false;
        }
    }

    public void onTaskSuccess() {
        tasksCompletedInStage++;
        startNextTaskInStage();
    }

    public void onTaskFailure() {
        broadcast(ChatColor.RED + "Task failed! Restarting " + currentStage.name() + " stage.");
        startStage(currentStage);
    }

    public boolean isRunning() {
        return running;
    }

    private void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}