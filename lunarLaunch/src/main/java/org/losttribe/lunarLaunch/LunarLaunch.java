package org.losttribe.lunarLaunch;

import org.losttribe.lunarLaunch.commands.LaunchCommand;
import org.losttribe.lunarLaunch.listeners.SetupListener;
import org.losttribe.lunarLaunch.managers.StageManager;
import org.losttribe.lunarLaunch.managers.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LunarLaunch extends JavaPlugin {

    private static LunarLaunch instance;

    private boolean setupMode = false;
    private StageManager stageManager;
    private TaskManager taskManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1) Ensure data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // 2) Check if config.yml exists
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();  // create empty file
                getLogger().info("Created an empty config.yml");
            } catch (IOException e) {
                getLogger().severe("Could not create config.yml!");
                e.printStackTrace();
            }
        }

        // 3) Now load it into the plugin's config
        //    (This is how Spigot loads a file named 'config.yml' in the data folder)
        reloadConfig();

        getLogger().info("Config loaded or created successfully!");

        // 2) Initialize managers
        taskManager = new TaskManager(this);
        stageManager = new StageManager(this, taskManager);

        // 3) Register /launch command
        getCommand("launch").setExecutor(new LaunchCommand(this, stageManager));

        // 4) Register the setup listener
        getServer().getPluginManager().registerEvents(new SetupListener(this), this);

        getLogger().info("LunarLaunch plugin enabled. config.yml loaded!");
    }

    @Override
    public void onDisable() {
        // Save any unsaved changes to config
        saveConfig();
    }

    public static LunarLaunch getInstance() {
        return instance;
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

    public TaskManager getTaskManager() {
        return taskManager;
    }
}