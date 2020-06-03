package ru.map;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.block.Block;
import ru.game.WorldManager;
import ru.util.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapFinale {

	private static List<Location> spawns = new ArrayList<>();
	private static Location winnerSpawn = new Location(null, 10, 13, 82);
	private static Region region = new Region(new Location(null, 0, 10, 80), new Location(null, 19, 22, 91));
	private static Region floor = new Region(new Location(null, 2, 10, 82), new Location(null, 17, 10, 89));

	static {
		spawns.add(new Location(null, 4, 11, 85));
		spawns.add(new Location(null, 7, 11, 87));
		spawns.add(new Location(null, 10, 11, 88));
		spawns.add(new Location(null, 13, 11, 87));
		spawns.add(new Location(null, 16, 11, 85));
	}

	public static Location getWinnerSpawn() {
		Location location = winnerSpawn.clone();
		location.setWorld(WorldManager.getCurrentGameWorld());
		return location;
	}

	public static Region getRegion() {
		Region copy = new Region(region);
		copy.setWorld(WorldManager.getCurrentGameWorld());
		return copy;
	}

	public static List<Location> getSpawns() {
		List<Location> list = Lists.newArrayList(spawns);
		list.forEach(location -> location.setWorld(WorldManager.getCurrentGameWorld()));
		return list;
	}

	public static Region getFloor() {
		Region clone = new Region(floor);
		clone.setWorld(WorldManager.getCurrentGameWorld());
		return clone;
	}

}
