package org.losttribe.lunarLaunch.models;

public enum LaunchStage {
    LIFTOFF("Liftoff", "You have 7s tasks to maintain the rocket."),
    CRUISE("Cruise", "You have 5s tasks. Keep stable orbit."),
    LANDING("Landing", "You have 3s tasks to land safely!");

    private final String displayName;
    private final String description;

    LaunchStage(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
