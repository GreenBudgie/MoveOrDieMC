package ru.modes;

import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import ru.game.*;
import ru.mutator.Mutator;
import ru.mutator.MutatorManager;
import ru.mutator.MutatorSelector;
import ru.util.Broadcaster;
import ru.util.MathUtils;
import ru.util.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModeManager implements Listener {

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
		if(TaskManager.isSecUpdated() && GameState.updateTimer()) {
			endRound();
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
		PlayerHandler.resurrectAll();
		PlayerHandler.clearDeathQueue();
		selectRandomMode().prepare();
		Mutator mutator = MutatorSelector.getSelectedMutator();
		if(mutator != null) {
			if(MutatorManager.getActiveMutator() == null || mutator != MutatorManager.getActiveMutator()) {
				mutator.setActive();
			}
		}
		if(MutatorManager.getActiveMutator() != null) {
			MutatorManager.getActiveMutator().onRoundPrepare();
		}
		GameState.setTimer(6);
		GameState.ROUND_START.set();
	}

	public static void beginRound() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "GO", "", 0, 20, 5);
			player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1F, 1.8F);
		}
		activeMode.start();
		if(activeMode.hasTime()) {
			GameState.setTimer(activeMode.getTime());
		} else {
			GameState.disableTimer();
		}
		if(MutatorManager.getActiveMutator() != null) {
			MutatorManager.getActiveMutator().onRoundStart();
		}
		GameState.GAME.set();
	}

	private static void addScore(Set<MDPlayer> players, int score) {
		for(MDPlayer player : players) {
			player.addScore(score);
			ScoreboardHandler.setAddedScore(player, score);
		}
	}

	public static void endRound() {
		if(MutatorManager.getActiveMutator() != null) {
			MutatorManager.getActiveMutator().onRoundPreEnd();
		}
		//FIXME Выдает не те очки, иногда выдает 0))))
		boolean hasAlive = !PlayerHandler.getAlive().isEmpty();
		List<Set<MDPlayer>> deathQueue = PlayerHandler.getDeathQueue();
		if(hasAlive) { //If some players are still alive, we need to add them to death queue so as to give them enough points
			deathQueue.add(Sets.newHashSet(PlayerHandler.getAlive()));
		}
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
		if(!hasAlive) {
			br.title(ChatColor.RED + "" + ChatColor.BOLD + "Ничья", ChatColor.GRAY + "Никто не победил", 5, 60, 10);
		} else {
			MDPlayer winner = deathQueue.get(deathQueue.size() - 1).iterator().next();
			br.title(winner.getColor() + winner.getNickname(), ChatColor.GREEN + "Победил", 5, 60, 10);
		}
		MoveOrDie.increaseRounds();
		GameState.setTimer(5);
		GameState.ROUND_END.set();
	}

	@EventHandler
	public void tntExplosion(EntityExplodeEvent e) {
		if(e.getEntityType() == EntityType.PRIMED_TNT) {
			e.blockList().clear();
		}
	}

}
