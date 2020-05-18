package ru.modes;

import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.MoveOrDie;
import ru.game.PlayerHandler;
import ru.util.Broadcaster;
import ru.util.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModeManager {

	private static Mode activeMode = null;
	private static Set<Mode> availableModes;
	private static List<Mode> modes = new ArrayList<>();
	public static ModeFight FIGHT = new ModeFight();

	public static List<Mode> getModes() {
		return modes;
	}

	public static Mode getByID(String ID) {
		return modes.stream().filter(mode -> mode.getID().equalsIgnoreCase(ID)).findFirst().orElse(null);
	}

	public static Mode getActiveMode() {
		return activeMode;
	}

	public static void update() {
		if(activeMode != null && activeMode.isActive()) {
			activeMode.update();
		}
	}

	public static void setup() {
		availableModes = Sets.newHashSet(modes);
	}

	public static void cleanup() {
		availableModes.clear();
	}

	public static Mode selectRandomMode() {
		if(availableModes.isEmpty()) {
			setup();
		}
		activeMode = MathUtils.choose(availableModes);
		availableModes.remove(activeMode);
		return activeMode;
	}

	public static void startNewRound() {
		PlayerHandler.clearDeathQueue();
		selectRandomMode().start();
		GameState.disableTimer();
		GameState.GAME.set();
	}

	private static void addScore(Set<MDPlayer> players, int score) {
		players.forEach(player -> player.addScore(score));
	}

	public static void endRound() {
		boolean draw = PlayerHandler.getAlive().isEmpty();
		List<Set<MDPlayer>> deathQueue = PlayerHandler.getDeathQueue();
		//Winners
		addScore(deathQueue.get(deathQueue.size() - 1), MoveOrDie.getWinnerScore());
		//Second place
		if(deathQueue.size() >= 2) {
			addScore(deathQueue.get(deathQueue.size() - 2), MoveOrDie.getSecondScore());
		}
		//Losers
		if(deathQueue.size() >= 3) {
			for(int i = 3; i <= deathQueue.size(); i++) {
				addScore(deathQueue.get(deathQueue.size() - i), MoveOrDie.getLoserScore());
			}
		}
		Broadcaster br = Broadcaster.each(PlayerHandler.getPlayers());
		if(draw) {
			br.title(ChatColor.RED + "" + ChatColor.BOLD + "Ничья", ChatColor.GRAY + "Никто не победил", 5, 60, 10);
		} else {
			MDPlayer winner = deathQueue.get(deathQueue.size() - 1).iterator().next();
			br.title(winner.getColor() + winner.getNickname(), ChatColor.GREEN + "Победил", 5, 60, 10);
		}
		GameState.setTimer(3);
		GameState.ROUND_END.set();
	}

}
