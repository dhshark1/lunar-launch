package org.losttribe.lunarLaunch;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LaunchCommand implements CommandExecutor {

    private final LaunchManager launchManager;

    public LaunchCommand(LaunchManager launchManager) {
        this.launchManager = launchManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Start the launch
        if (label.equalsIgnoreCase("launch")) {
            if (launchManager.isLaunching()) {
                sender.sendMessage(ChatColor.RED + "Launch is already in progress!");
            } else {
                launchManager.startLaunchSequence();
                sender.sendMessage(ChatColor.GREEN + "Rocket launch sequence initiated!");
            }
            return true;
        }

        return false;
    }
}