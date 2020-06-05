package ru.modes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import ru.game.*;
import ru.mutator.Mutator;
import ru.mutator.MutatorManager;
import ru.mutator.MutatorSelector;
import ru.util.Broadcaster;
import ru.util.MathUtils;
import ru.util.TaskManager;

import javax.annotation.Nullable;
import java.util.*;

public class ModeManager implements Listener {

	private static Mode activeMode = null;
	private static Set<Mode> availableModes;
	private static List<Mode> modes = new ArrayList<>();
	private static boolean suddenDeath = false;
	public static ModeFight FIGHT = new ModeFight();
	public static ModeSpleef SPLEEF = new ModeSpleef();
	public static ModeDangerBuilder DANGER_BUILDER = new ModeDangerBuilder();
	public static ModeFall FALL = new ModeFall();
	public static ModeBombTag BOMB_TAG = new ModeBombTag();
	public static ModeCrossbower CROSSBOWER = new ModeCrossbower();
	public static ModeShiftyGround SHIFTY_GROUND = new ModeShiftyGround();
	public static ModeAnvilFall ANVIL_FALL = new ModeAnvilFall();

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
			if(activeMode.allowSuddenDeath()) {
				initSuddenDeath();
				GameState.disableTimer();
			} else {
				endRound();
			}
		}
	}

	public static void setup() {
		availableModes = Sets.newHashSet(modes);
	}

	public static void cleanup() {
		availableModes.clear();
		suddenDeath = false;
		if(activeMode != null) {
			activeMode.onRoundEnd();
			if(activeMode instanceof Listener) {
				HandlerList.unregisterAll((Listener) activeMode);
			}
		}
	}

	public static boolean isSuddenDeath() {
		return suddenDeath;
	}

	public static void initSuddenDeath() {
		if(!suddenDeath) {
			suddenDeath = true;
			for(Player player : PlayerHandler.getPlayers()) {
				player.sendTitle("", ChatColor.DARK_RED + "" + ChatColor.BOLD + "Внезапная смерть!", 5, 30, 5);
				player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1F, 1F);
			}
		}
	}

	public static void selectRandomMode() {
		if(availableModes.isEmpty()) {
			setup();
		}
		activeMode = MathUtils.choose(availableModes);
		availableModes.remove(activeMode);
	}

	public static void startNewRound() {
		startNewRound(null);
	}

	public static void startNewRound(@Nullable Mode mode) {
		PlayerHandler.resurrectAll();
		PlayerHandler.clearDeathQueue();
		if(mode == null) {
			selectRandomMode();
		} else {
			activeMode = mode;
		}
		activeMode.prepare();
		Mutator mutator = MutatorSelector.getSelectedMutator();
		if(MutatorManager.FORCED_MUTATOR != null) {
			mutator = MutatorManager.FORCED_MUTATOR;
			MutatorManager.FORCED_MUTATOR = null;
		}
		if(MutatorManager.FORCE_DISABLE) {
			mutator = null;
		}
		if(mutator != null) {
			if(MutatorManager.getActiveMutator() == null || mutator != MutatorManager.getActiveMutator()) {
				mutator.setActive();
			}
		}
		if(MutatorManager.getActiveMutator() != null) {
			MutatorManager.getActiveMutator().onRoundPrepare();
		}
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			mdPlayer.resetPoints();
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

	public static void endRound() {
		if(MutatorManager.getActiveMutator() != null) {
			MutatorManager.getActiveMutator().onRoundPreEnd();
		}
		activeMode.end();
		suddenDeath = false;

		List<MDPlayer> alive = PlayerHandler.getAlive();
		boolean draw = alive.size() != 1;
		List<Set<MDPlayer>> winQueue = Lists.newArrayList(PlayerHandler.getDeathQueue());
		if(!alive.isEmpty()) { //If some players are still alive, we need to add them to win queue so as to give them enough points
			if(activeMode.usePoints()) {
				//Adding winners by points from LESS points to MOST (because afterwards the list will be reversed)
				alive.sort(Comparator.comparingInt(MDPlayer::getPoints));
				Set<MDPlayer> currentPointsSet = new HashSet<>();
				int currentPoints = -1;
				for(MDPlayer currentPlayer : alive) {
					if(currentPlayer.getPoints() > currentPoints) {
						//Adding new players to win queue
						if(!currentPointsSet.isEmpty()) winQueue.add(Sets.newHashSet(currentPointsSet));
						currentPointsSet.clear();
						currentPoints = currentPlayer.getPoints();
						currentPointsSet.add(currentPlayer);
					} else {
						currentPointsSet.add(currentPlayer);
					}
				}
				winQueue.add(Sets.newHashSet(currentPointsSet));
			} else {
				winQueue.add(Sets.newHashSet(PlayerHandler.getAlive()));
			}
		}
		Collections.reverse(winQueue); //Now the winner is on 0 place
		Broadcaster br = Broadcaster.each(PlayerHandler.getPlayers());
		br.toChat(ChatColor.GRAY + "-- " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Результаты" + ChatColor.RESET + ChatColor.GRAY + " --");
		for(int i = 0; i < winQueue.size(); i++) {
			Set<MDPlayer> placePlayers = winQueue.get(i);
			int score = MoveOrDie.getScoreForPlace(i + 1);
			for(MDPlayer player : placePlayers) {
				player.addScore(score, !draw && alive.contains(player));
			}
			StringBuilder builder = new StringBuilder();
			for(MDPlayer currentPlayer : placePlayers) {
				builder.append(currentPlayer.getColor());
				builder.append(currentPlayer.getNickname());
				builder.append(ChatColor.GRAY);
				builder.append(", ");
			}
			String info = builder.toString().substring(0, builder.length() - 2);
			String points = activeMode.usePoints() && !placePlayers.iterator().next().isGhost() ?
					ChatColor.GRAY + " <" + ChatColor.DARK_AQUA + "" + placePlayers.iterator().next().getPoints() + ChatColor.GRAY + ">" :
					"";
			br.toChat(ChatColor.DARK_GREEN + " #" + ChatColor.GREEN + (i + 1) + " " + info + points + ChatColor.DARK_GRAY + " (" + ChatColor.DARK_GREEN + "+" + score
					+ ChatColor.DARK_GRAY + ")");
		}
		if(draw) {
			br.title(ChatColor.RED + "" + ChatColor.BOLD + "Ничья", ChatColor.GRAY + "Нет победителя", 5, 60, 10);
		} else {
			MDPlayer winner = winQueue.get(0).iterator().next();
			br.title(winner.getColor() + winner.getNickname(), ChatColor.GREEN + "Победил", 5, 60, 10);
			Rating.ROUNDS_WON.increaseValue(winner.getNickname());
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

	@EventHandler
	public void ghostAttack(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			MDPlayer victim = MDPlayer.fromPlayer((Player) e.getEntity());
			MDPlayer damager = MDPlayer.fromPlayer((Player) e.getDamager());
			if(victim != null && damager != null) {
				if(victim.isGhost() || damager.isGhost()) e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent e) {
		if(PlayerHandler.isPlaying(e.getPlayer())) {
			if(!GameState.GAME.isRunning()) {
				e.setCancelled(true);
			} else {
				if(!activeMode.allowBlockPlacing()) e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		if(PlayerHandler.isPlaying(e.getPlayer())) {
			if(!GameState.GAME.isRunning()) {
				e.setCancelled(true);
			} else {
				if(!activeMode.allowBlockBreaking()) e.setCancelled(true);
			}
		}
	}

}
