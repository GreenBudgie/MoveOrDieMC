package ru.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.lobby.LobbyEntertainmentHandler;
import ru.lobby.LobbyParkourHandler;
import ru.lobby.sign.LobbySignManager;
import ru.map.MapManager;
import ru.modes.ModeManager;
import ru.mutator.MutatorManager;
import ru.mutator.MutatorSelector;
import ru.start.Plugin;
import ru.util.Broadcaster;
import ru.util.MathUtils;
import ru.util.TaskManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MoveOrDie implements Listener {

	public static final boolean TEST = true; //FIXME Remove on release
	public static final int MAX_PLAYERS = 6, MIN_PLAYERS = 3;
	private static final Set<ChatColor> availableColors = Sets
			.newHashSet(ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GOLD);
	private static int roundsPassed = 0, roundsToSelectNewMutator = 6;
	private static int scoreToWin = 25; //TODO

	public static void init() {
		Bukkit.getPluginManager().registerEvents(new MoveOrDie(), Plugin.INSTANCE);
		Bukkit.getPluginManager().registerEvents(new PlayerHandler(), Plugin.INSTANCE);
		WorldManager.init();
		TaskManager.init();
		LobbyParkourHandler.init();
		LobbyEntertainmentHandler.init();
		LobbySignManager.init();
		MapManager.init();
		ScoreboardHandler.createLobbyScoreboard();
	}

	public static void startGame() {
		if(!GameState.isPlaying()) {
			Broadcaster.everybody().title(ChatColor.RED + "" + ChatColor.BOLD + "Игра" + ChatColor.BLUE + " Начинается", "", 10, 40, 10);
			WorldManager.makeWorld();

			List<Player> shuffled = Lists.newArrayList(Bukkit.getOnlinePlayers());
			Collections.shuffle(shuffled);
			int count = Math.min(MAX_PLAYERS, shuffled.size());
			Set<ChatColor> colors = Sets.newHashSet(MoveOrDie.getAvailableColors());
			for(int i = 0; i < count; i++) {
				Player player = shuffled.get(i);
				MDPlayer mdPlayer = new MDPlayer(player);
				PlayerHandler.getMDPlayers().add(mdPlayer);
				//Choosing random color
				ChatColor color = MathUtils.choose(colors);
				mdPlayer.setColor(color);
				colors.remove(color);
				//Setting up for teleport
				PlayerHandler.reset(player);
				PlayerHandler.givePlayerEffects(player);
				player.setGameMode(GameMode.ADVENTURE);
				ScoreboardHandler.createGameScoreboard(player);
			}
			GameSetupManager.start();
			LobbySignManager.updateSigns();
			roundsPassed = 0;
			roundsToSelectNewMutator = 6;
		}
	}

	public static void endGame() {
		if(GameState.isPlaying()) {
			for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
				Player player = mdPlayer.getPlayer();
				player.setGameMode(GameMode.SURVIVAL);
				PlayerHandler.reset(player);
				player.teleport(WorldManager.getLobby().getSpawnLocation());
				player.setInvulnerable(false);
				mdPlayer.cleanup();
			}
			WorldManager.deleteWorld();
			PlayerHandler.getMDPlayers().clear();
			GameState.STOPPED.set();
			LobbySignManager.updateSigns();
			ScoreboardHandler.updateScoreboardTeams();
			ModeManager.cleanup();
			MutatorSelector.cleanup();
		}
	}

	public static void update() {
		LobbyParkourHandler.update();
		if(GameState.isPlaying()) {
			PlayerHandler.update();
			if(TaskManager.ticksPassed(10)) ScoreboardHandler.updateGameTeams();
		}
		if(GameState.SETUP.isRunning()) {
			GameSetupManager.update();
		}
		if(GameState.MUTATOR.isRunning()) {
			MutatorSelector.update();
		}
		if(GameState.GAME.isRunning()) {
			ModeManager.update();
			MutatorManager.update();
		}
		if(GameState.ROUND_END.isRunning()) {
			if(TaskManager.isSecUpdated() && GameState.updateTimer()) {
				MDPlayer winner = null;
				for(MDPlayer player : PlayerHandler.getMDPlayers()) {
					if(player.getScore() >= getScoreToWin()) {
						winner = player;
						break;
					}
				}
				if(winner != null) {
					GameFinaleManager.start(winner);
				} else {
					if(roundsPassed % roundsToSelectNewMutator == 0) {
						MutatorSelector.start();
					} else {
						ModeManager.startNewRound();
					}
				}
			}
		}
		if(GameState.FINALE.isRunning()) {
			GameFinaleManager.update();
		}
	}

	public static int getScoreToWin() {
		return scoreToWin;
	}

	public static int getWinnerScore() {
		return PlayerHandler.getMDPlayers().size() + 2;
	}

	public static int getSecondScore() {
		return (int) Math.ceil(getWinnerScore() / 2.5);
	}

	public static int getLoserScore() {
		return (int) Math.ceil(getWinnerScore() / 4.0);
	}

	public static Set<ChatColor> getAvailableColors() {
		return availableColors;
	}

	public static String getLogo() {
		return ChatColor.RED + "" + ChatColor.BOLD + "Move" + ChatColor.RESET + ChatColor.YELLOW + " or " + ChatColor.RESET + ChatColor.BLUE
				+ ChatColor.BOLD + "Die";
	}

}
