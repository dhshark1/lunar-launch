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

    private final int timeToComplete;  // in seconds
    private BukkitTask timeoutTask;

    private final MaintenanceManager maintenanceManager =
            LunarLaunch.getInstance().getMaintenanceManager();

    // Actions that players can perform in this plugin
    public enum TaskAction {
        PRESS_BUTTON,
        FLICK_LEVER,
        PLACE_REDSTONE,
        PLACE_IRON_BLOCK
    }

    /**
     * Creates a new multi-step maintenance task with N random steps (2-4).
     */
    public MaintenanceTask(int timeToComplete) {
        this.timeToComplete = timeToComplete;
        this.requiredSteps = generateRandomSteps();
        this.name = "Multi-step Task (" + requiredSteps.size() + " steps)";
    }

    /**
     * Start the task:
     *  - Announce instructions
     *  - Schedule a timeout
     */
    public void start() {
        broadcast(ChatColor.RED + "MAINTENANCE REQUIRED: " + ChatColor.YELLOW + getInstructions());
        scheduleTimeout();
    }

    /**
     * Called by MaintenanceManager / TaskListener when a player performs an action.
     */
    public void progressTask(TaskAction action) {
        if (completed) return;

        // Check if the player performed the correct action for the current step
        TaskAction required = requiredSteps.get(currentStepIndex);
        if (action == required) {
            currentStepIndex++;
            // If all steps are completed
            if (currentStepIndex >= requiredSteps.size()) {
                complete();
            } else {
                broadcast(ChatColor.AQUA + "Good job! Next step: "
                        + ChatColor.YELLOW + formatStep(requiredSteps.get(currentStepIndex)));
            }
        } else {
            // Wrong action -> reset the step progress
            broadcast(ChatColor.RED + "Incorrect action for this step! Progress reset to step 1.");
            currentStepIndex = 0;
        }
    }

    /**
     * Mark this task as completed
     */
    private void complete() {
        completed = true;
        cancelTimeout(); // no longer need the timeout
    }

    /**
     * Returns true if the task is completed
     */
    public boolean isCompleted() {
        return completed;
    }

    public String getName() {
        return name;
    }

    /**
     * Cancel the scheduled timeout (used on completion or when tasks are reset)
     */
    public void cancelTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
    }

    /**
     * Generate the instructions for display: “Step 1: FLICK_LEVER, Step 2: PRESS_BUTTON...”
     */
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

    /**
     * Simple translator from TaskAction enum to a user-friendly string
     */
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

    /**
     * Randomly generate 2-4 steps
     */
    private List<TaskAction> generateRandomSteps() {
        List<TaskAction> possibleActions = List.of(
                TaskAction.PRESS_BUTTON,
                TaskAction.FLICK_LEVER,
                TaskAction.PLACE_REDSTONE,
                TaskAction.PLACE_IRON_BLOCK
        );

        Random rand = new Random();
        int stepCount = rand.nextInt(3) + 2; // 2–4 steps
        List<TaskAction> steps = new ArrayList<>();
        for (int i = 0; i < stepCount; i++) {
            steps.add(possibleActions.get(rand.nextInt(possibleActions.size())));
        }
        return steps;
    }

    /**
     * Schedule a timeout that fails the entire launch if not completed in time
     */
    private void scheduleTimeout() {
        timeoutTask = Bukkit.getScheduler().runTaskLater(
                LunarLaunch.getInstance(),
                this::onTimeout,
                timeToComplete * 20L
        );
    }

    /**
     * Called if time runs out
     */
    private void onTimeout() {
        if (!completed) {
            // Fail the entire launch
            maintenanceManager.failTask(this);
        }
    }

    private void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}
