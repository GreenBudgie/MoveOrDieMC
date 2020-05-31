package ru.map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.blocks.CustomBlockTeleporter;
import ru.game.MoveOrDie;
import ru.game.WorldManager;
import ru.modes.Mode;
import ru.modes.ModeManager;
import ru.start.Plugin;
import ru.util.Broadcaster;
import ru.util.MathUtils;
import ru.util.Region;
import ru.util.WorldUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MapManager {

	private static List<GameMap> maps = new ArrayList<>();
	private static File mapsFile = new File(WorldManager.getOriginalGameWorld().getWorldFolder() + File.separator + "maps.yml");
	private static YamlConfiguration mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);
	private static Set<GameMap> usedMaps = new HashSet<>();

	@SuppressWarnings (value="unchecked")
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
				if(section.contains("teleporters")) {
					try {
						List<?> list = section.getList("teleporters");
						if(list != null) {
							for(Object teleporter : list) {
								List<String> linked = (List<String>) teleporter;
								Location l1 = WorldUtils.getLocationFromString(linked.get(0));
								Location l2 = WorldUtils.getLocationFromString(linked.get(1));
								CustomBlockTeleporter.linkTeleporters(l1, l2);
							}
						} else throw new RuntimeException("Invalid teleporters declaration");
					} catch(Exception e) {
						throw new RuntimeException("Invalid teleporters declaration");
					}
				}
				if(modes.isEmpty()) throw new RuntimeException("A map cannot support no modes");
				Region region = Region.deserialize(section.getConfigurationSection("region").getValues(false));
				if(region == null) throw new RuntimeException("Invalid region");
				GameMap map = new GameMap();
				map.setSpawns(locations);
				map.setSupportedModes(modes);
				map.setRegion(region);
				for(Mode mode : modes) {
					ConfigurationSection modeSection  = section.getConfigurationSection(mode.getID());
					if(modeSection != null) {
						mode.deserializeMapOptions(map, modeSection);
					}
				}
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
