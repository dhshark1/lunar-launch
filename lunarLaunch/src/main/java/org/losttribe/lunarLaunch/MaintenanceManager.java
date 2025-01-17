package org.losttribe.lunarLaunch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceManager {

    private static final int TASK_COMPLETION_TIME_SECONDS = 20;

    private final List<MaintenanceTask> activeTasks = new ArrayList<>();

    public void spawnRandomMaintenanceTask() {
        MaintenanceTask task = new MaintenanceTask(TASK_COMPLETION_TIME_SECONDS);
        activeTasks.add(task);
        task.start();
    }

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

    public void failTask(MaintenanceTask task) {
        activeTasks.remove(task);

        LunarLaunch.getInstance().getLaunchManager().failLaunch();
    }

    private void completeTask(MaintenanceTask task) {
        broadcast(ChatColor.GREEN + "Maintenance Task COMPLETED: " + task.getName());
        activeTasks.remove(task);
    }

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

