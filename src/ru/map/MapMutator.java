package ru.map;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import ru.game.WorldManager;

import java.util.ArrayList;
import java.util.List;

public class MapMutator {

	private static List<Location> spawns = new ArrayList<>();
	private static Location mutatorSelectorSpawn = new Location(null, 9, 11, 65);

	static {
		spawns.add(new Location(null, 11, 11, 67));
		spawns.add(new Location(null, 8, 11, 68));
		spawns.add(new Location(null, 5, 11, 65));
		spawns.add(new Location(null, 9, 11, 61));
		spawns.add(new Location(null, 12, 11, 63));
		spawns.add(new Location(null, 6, 11, 62));
	}

	public static Location getMutatorSelectorSpawn() {
		Location location = mutatorSelectorSpawn.clone();
		location.setWorld(WorldManager.getCurrentGameWorld());
		return location;
	}

	public static List<Location> getSpawns() {
		List<Location> list = Lists.newArrayList(spawns);
		list.forEach(location -> location.setWorld(WorldManager.getCurrentGameWorld()));
		return list;
	}
}
