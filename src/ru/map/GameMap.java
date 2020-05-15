package ru.map;

import org.bukkit.Location;
import ru.modes.Mode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameMap {

	private List<Location> spawns = new ArrayList<>();
	private Set<Mode> supportedModes = new HashSet<>();

	public List<Location> getSpawns() {
		return spawns;
	}

	public void setSpawns(List<Location> spawns) {
		this.spawns = spawns;
	}

	public Set<Mode> getSupportedModes() {
		return supportedModes;
	}

	public void setSupportedModes(Set<Mode> supportedModes) {
		this.supportedModes = supportedModes;
	}
}
