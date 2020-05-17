package ru.game;

import com.google.common.collect.Lists;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.map.MapFinale;
import ru.map.MapMutator;
import ru.util.EntityUtils;

import java.util.Collections;
import java.util.List;

public class GameFinaleManager {

	public static void start() {
		List<Location> locations = Lists.newArrayList(MapFinale.getSpawns());
		Collections.shuffle(locations);
		//Add winner
		for(int i = 0; i < PlayerHandler.getPlayers().size(); i++) {
			Player player = PlayerHandler.getPlayers().get(i);
			PlayerHandler.resetNoEffects(player);
			player.setGameMode(GameMode.ADVENTURE);
			EntityUtils.teleportCentered(player, locations.get(i), true, true);
		}
		GameState.setTimer(10);
		GameState.FINALE.set();
	}

}
