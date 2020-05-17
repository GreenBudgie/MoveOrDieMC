package ru.map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import ru.game.WorldManager;

import java.util.HashMap;
import java.util.Map;

public class MapSetup {

	private static Map<ChatColor, Location> spawns = new HashMap<>();

	static {
		spawns.put(ChatColor.GOLD, new Location(null, 15, 16, 39));
		spawns.put(ChatColor.YELLOW, new Location(null, 9, 14, 39));
		spawns.put(ChatColor.RED, new Location(null, 3, 16, 39));
		spawns.put(ChatColor.AQUA, new Location(null, 15, 14, 45));
		spawns.put(ChatColor.LIGHT_PURPLE, new Location(null, 3, 14, 45));
		spawns.put(ChatColor.GREEN, new Location(null, 9, 12, 45));
	}

	public static Location getSpawn(ChatColor color) {
		Location location = spawns.get(color).clone();
		location.setWorld(WorldManager.getCurrentGameWorld());
		return location;
	}

}
