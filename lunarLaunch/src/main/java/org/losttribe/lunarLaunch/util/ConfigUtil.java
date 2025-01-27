package org.losttribe.lunarLaunch.util;

import org.losttribe.lunarLaunch.LunarLaunch;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {

    private static List<Location> loadedLocations = new ArrayList<>();

    public static void loadBlocksFromConfig() {
        FileConfiguration config = LunarLaunch.getInstance().getConfig();
        loadedLocations.clear();

        if (!config.contains("tasks.blocks")) {
            return;
        }
        List<String> locStrings = config.getStringList("tasks.blocks");
        for (String locString : locStrings) {
            Location loc = stringToLocation(locString);
            if (loc != null) {
                loadedLocations.add(loc);
            }
        }
    }

    public static void addBlockLocation(Location loc) {
        FileConfiguration config = LunarLaunch.getInstance().getConfig();
        List<String> locStrings = config.getStringList("tasks.blocks");
        locStrings.add(locationToString(loc));
        config.set("tasks.blocks", locStrings);
        LunarLaunch.getInstance().saveConfig();

        // Add to in-memory list
        loadedLocations.add(loc);
    }

    public static List<Location> getBlockLocations() {
        return loadedLocations;
    }

    private static String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private static Location stringToLocation(String s) {
        String[] parts = s.split(",");
        if (parts.length != 4) return null;
        return new Location(
                Bukkit.getWorld(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }
}
