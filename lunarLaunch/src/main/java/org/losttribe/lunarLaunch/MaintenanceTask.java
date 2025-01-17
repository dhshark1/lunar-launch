package org.losttribe.lunarLaunch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MaintenanceTask {

    private final String name;
    private final List<TaskAction> requiredSteps;
    private int currentStepIndex = 0;
    private boolean completed = false;

    private final int timeToComplete;
    private BukkitTask timeoutTask;

    private final MaintenanceManager maintenanceManager =
            LunarLaunch.getInstance().getMaintenanceManager();

    public enum TaskAction {
        PRESS_BUTTON,
        FLICK_LEVER,
        PLACE_REDSTONE,
        PLACE_IRON_BLOCK
    }

    public MaintenanceTask(int timeToComplete) {
        this.timeToComplete = timeToComplete;
        this.requiredSteps = generateRandomSteps();
        this.name = "Multi-step Task (" + requiredSteps.size() + " steps)";
    }

    public void start() {
        broadcast(ChatColor.RED + "MAINTENANCE REQUIRED: " + ChatColor.YELLOW + getInstructions());
        scheduleTimeout();
    }

    public void progressTask(TaskAction action) {
        if (completed) return;

        TaskAction required = requiredSteps.get(currentStepIndex);
        if (action == required) {
            currentStepIndex++;
            if (currentStepIndex >= requiredSteps.size()) {
                complete();
            } else {
                broadcast(ChatColor.AQUA + "Good job! Next step: "
                        + ChatColor.YELLOW + formatStep(requiredSteps.get(currentStepIndex)));
            }
        } else {
            broadcast(ChatColor.RED + "Incorrect action for this step! Progress reset to step 1.");
            currentStepIndex = 0;
        }
    }

    private void complete() {
        completed = true;
        cancelTimeout();
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getName() {
        return name;
    }

    public void cancelTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
    }

    private String getInstructions() {
        StringBuilder sb = new StringBuilder("Complete the following steps **in order**:\n");
        for (int i = 0; i < requiredSteps.size(); i++) {
            sb.append(ChatColor.YELLOW)
                    .append("Step ")
                    .append(i + 1)
                    .append(": ")
                    .append(formatStep(requiredSteps.get(i)))
                    .append("\n");
        }
        return sb.toString();
    }

    private String formatStep(TaskAction action) {
        switch (action) {
            case PRESS_BUTTON:
                return "Press a Button";
            case FLICK_LEVER:
                return "Flick a Lever";
            case PLACE_REDSTONE:
                return "Place Redstone Wire";
            case PLACE_IRON_BLOCK:
                return "Place an Iron Block";
        }
        return action.name();
    }

    private List<TaskAction> generateRandomSteps() {
        List<TaskAction> possibleActions = List.of(
                TaskAction.PRESS_BUTTON,
                TaskAction.FLICK_LEVER,
                TaskAction.PLACE_REDSTONE,
                TaskAction.PLACE_IRON_BLOCK
        );

        Random rand = new Random();
        int stepCount = rand.nextInt(3) + 2;
        List<TaskAction> steps = new ArrayList<>();
        for (int i = 0; i < stepCount; i++) {
            steps.add(possibleActions.get(rand.nextInt(possibleActions.size())));
        }
        return steps;
    }

    private void scheduleTimeout() {
        timeoutTask = Bukkit.getScheduler().runTaskLater(
                LunarLaunch.getInstance(),
                this::onTimeout,
                timeToComplete * 20L
        );
    }

    private void onTimeout() {
        if (!completed) {
            maintenanceManager.failTask(this);
        }
    }

    private void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}
