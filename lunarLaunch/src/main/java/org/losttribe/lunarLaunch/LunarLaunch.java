package org.losttribe.lunarLaunch;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LunarLaunch extends JavaPlugin {

    private static LunarLaunch instance;

    private LaunchManager launchManager;
    private MaintenanceManager maintenanceManager;

    public static LunarLaunch getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.maintenanceManager = new MaintenanceManager();
        this.launchManager = new LaunchManager(maintenanceManager);

        // Register our events
        Bukkit.getPluginManager().registerEvents(new TaskListener(maintenanceManager), this);

        // Register /launch command
        getCommand("launch").setExecutor(new LaunchCommand(launchManager));

    }

    @Override
    public void onDisable() {
        // Clean up any tasks
        launchManager.stopAllTasks();
        maintenanceManager.stopAllTasks();
    }

    public LaunchManager getLaunchManager() {
        return launchManager;
    }

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }
}
