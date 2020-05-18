package ru.game;

import com.google.common.collect.Lists;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.map.MapFinale;
import ru.util.EntityUtils;
import ru.util.TaskManager;

import java.util.Collections;
import java.util.List;

public class GameFinaleManager {

	public static void start(MDPlayer winner) {
		List<Location> locations = Lists.newArrayList(MapFinale.getSpawns());
		Collections.shuffle(locations);
		for(int i = 0; i < PlayerHandler.getMDPlayers().size(); i++) {
			MDPlayer mdPlayer = PlayerHandler.getMDPlayers().get(i);
			if(mdPlayer == winner) continue;
			Player player = mdPlayer.getPlayer();
			PlayerHandler.resetNoEffects(player);
			player.setGameMode(GameMode.ADVENTURE);
			EntityUtils.teleportCentered(player, locations.get(i), true, true);
		}
		EntityUtils.teleport(winner.getPlayer(), MapFinale.getWinnerSpawn(), true, true);
		GameState.setTimer(10);
		GameState.FINALE.set();
	}

	public static void update() {
		if(TaskManager.isSecUpdated() && GameState.updateTimer()) {
			MoveOrDie.endGame();
		}
	}

}
