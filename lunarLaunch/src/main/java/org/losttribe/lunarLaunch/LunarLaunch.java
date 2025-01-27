package org.losttribe.lunarLaunch;

import org.losttribe.lunarLaunch.commands.LaunchCommand;
import org.losttribe.lunarLaunch.listeners.LampBlockListener;
import org.losttribe.lunarLaunch.listeners.SetupListener;
import org.losttribe.lunarLaunch.managers.StageManager;
import org.losttribe.lunarLaunch.managers.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LunarLaunch extends JavaPlugin {

    private boolean setupMode = false;
    private StageManager stageManager;
    private TaskManager taskManager;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        reloadConfig();

        taskManager = new TaskManager(this);
        stageManager = new StageManager(this, taskManager);

        getCommand("launch").setExecutor(new LaunchCommand(this, stageManager));

        getServer().getPluginManager().registerEvents(new SetupListener(this), this);
        getServer().getPluginManager().registerEvents(new LampBlockListener(taskManager), this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public boolean isSetupMode() {
        return setupMode;
    }

    public void setSetupMode(boolean setup) {
        this.setupMode = setup;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

}