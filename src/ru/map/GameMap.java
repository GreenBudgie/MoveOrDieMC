package ru.map;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.modes.Mode;
import ru.util.EntityUtils;

import java.util.*;

public class GameMap {

	private List<Location> spawns = new ArrayList<>();
	private Set<Mode> supportedModes = new HashSet<>();

	public List<Location> getSpawns() {
		List<Location> list = Lists.newArrayList(spawns);
		list.forEach(location -> location.setWorld(WorldManager.getCurrentGameWorld()));
		return list;
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

	public void spreadPlayers() {
		List<Location> shuffled = Lists.newArrayList(getSpawns());
		Collections.shuffle(shuffled);
		for(int i = 0; i < PlayerHandler.getPlayers().size(); i++) {
			Player player = PlayerHandler.getPlayers().get(i);
			EntityUtils.teleportCentered(player, shuffled.get(i), true, true);
		}
	}

}
