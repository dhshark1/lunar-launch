package org.losttribe.lunarLaunch.commands;

import org.losttribe.lunarLaunch.LunarLaunch;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.losttribe.lunarLaunch.managers.StageManager;

public class LaunchCommand implements CommandExecutor {

    private final LunarLaunch plugin;
    private final StageManager stageManager;

    public LaunchCommand(LunarLaunch plugin, StageManager stageManager) {
        this.plugin = plugin;
        this.stageManager = stageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /launch <start|setup>");
            return true;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (stageManager.isRunning()) {
                sender.sendMessage(ChatColor.RED + "Cannot enter setup mode after the launch has started!");
                return true;
            }

            boolean wasSetup = plugin.isSetupMode();
            plugin.setSetupMode(!wasSetup);
            if (!wasSetup) {
                sender.sendMessage(ChatColor.GREEN + "Setup mode enabled. Right-click a lever/button/plate on top of a redstone lamp.");
            } else {
                sender.sendMessage(ChatColor.RED + "Setup mode disabled.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (stageManager.isRunning()) {
                sender.sendMessage(ChatColor.RED + "The launch sequence is already running!");
            } else {
                // DISABLE SETUP on start
                plugin.setSetupMode(false);

                sender.sendMessage(ChatColor.GREEN + "Starting the multi-stage launch sequence...");
                stageManager.startLaunchFlow();
            }
            return true;
        }

        // Unknown subcommand
        sender.sendMessage(ChatColor.YELLOW + "Usage: /launch <start|setup>");
        return true;
    }
}