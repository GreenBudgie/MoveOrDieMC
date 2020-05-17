package ru.map;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import ru.game.WorldManager;

import java.util.ArrayList;
import java.util.List;

public class MapFinale {

	private static List<Location> spawns = new ArrayList<>();
	private static Location winnerSpawn = new Location(null, 10, 13, 82);

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

	public static List<Location> getSpawns() {
		List<Location> list = Lists.newArrayList(spawns);
		list.forEach(location -> location.setWorld(WorldManager.getCurrentGameWorld()));
		return list;
	}
}
