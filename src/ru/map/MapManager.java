package ru.map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.game.WorldManager;
import ru.modes.Mode;
import ru.modes.ModeManager;
import ru.start.Plugin;
import ru.util.WorldUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapManager {

	private static List<GameMap> maps = new ArrayList<>();
	private static File mapsFile = new File(WorldManager.getOriginalGameWorld().getWorldFolder() + File.separator + "maps.yml");
	private static YamlConfiguration mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);

	public static void init() {
		for(String key : mapsConfig.getKeys(false)) {
			ConfigurationSection section = mapsConfig.getConfigurationSection(key);
			if(section != null) {
				List<Location> locations = new ArrayList<>();
				for(String coords : section.getStringList("spawns")) {
					locations.add(WorldUtils.getLocationFromString(coords));
				}
				Set<Mode> modes = new HashSet<>();
				for(String modeID : section.getStringList("modes")) {
					Mode mode = ModeManager.getByID(modeID);
					if(mode != null) modes.add(ModeManager.getByID(modeID));
				}
				GameMap map = new GameMap();
				map.setSpawns(locations);
				map.setSupportedModes(modes);
				maps.add(map);
			}
		}
	}

}
