package ru.map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.game.MoveOrDie;
import ru.game.WorldManager;
import ru.modes.Mode;
import ru.modes.ModeManager;
import ru.start.Plugin;
import ru.util.MathUtils;
import ru.util.Region;
import ru.util.WorldUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapManager {

	private static List<GameMap> maps = new ArrayList<>();
	private static File mapsFile = new File(WorldManager.getOriginalGameWorld().getWorldFolder() + File.separator + "maps.yml");
	private static YamlConfiguration mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);
	private static Set<GameMap> usedMaps = new HashSet<>();

	public static void init() {
		for(String key : mapsConfig.getKeys(false)) {
			ConfigurationSection section = mapsConfig.getConfigurationSection(key);
			if(section != null) {
				List<Location> locations = new ArrayList<>();
				for(String coords : section.getStringList("spawns")) {
					locations.add(WorldUtils.getLocationFromString(coords));
				}
				if(locations.size() != MoveOrDie.MAX_PLAYERS) throw new RuntimeException("Not enough spawn location on a map");
				Set<Mode> modes = new HashSet<>();
				for(String modeID : section.getStringList("modes")) {
					Mode mode = ModeManager.getByID(modeID);
					if(mode == null) throw new RuntimeException("Invalid mode ID");
					modes.add(ModeManager.getByID(modeID));
				}
				if(modes.isEmpty()) throw new RuntimeException("A map cannot support no modes");
				Region region = Region.deserialize(section.getConfigurationSection("region").getValues(false));
				if(region == null) throw new RuntimeException("Invalid region");
				GameMap map = new GameMap();
				map.setSpawns(locations);
				map.setSupportedModes(modes);
				map.setRegion(region);
				maps.add(map);
			}
		}
	}

	public static List<GameMap> getMaps() {
		return maps;
	}

	public static GameMap useMapForMode(Mode mode) {
		GameMap gameMap = MathUtils.choose(maps.stream().filter(map -> map.getSupportedModes().contains(mode)).collect(Collectors.toList()));
		if(usedMaps.contains(gameMap)) {
			gameMap.reset();
		} else {
			usedMaps.add(gameMap);
		}
		return gameMap;
	}

	public static void cleanup() {
		usedMaps.clear();
	}

}
