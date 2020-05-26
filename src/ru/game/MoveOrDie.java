package ru.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import ru.blocks.CustomBlockManager;
import ru.lobby.LobbyEntertainmentHandler;
import ru.lobby.LobbyParkourHandler;
import ru.lobby.sign.LobbySignManager;
import ru.map.MapManager;
import ru.modes.ModeManager;
import ru.mutator.Mutator;
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

	public static boolean DO_MOVE_DAMAGE = true;
	public static final int MAX_PLAYERS = 6;
	private static final Set<ChatColor> availableColors = Sets
			.newHashSet(ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GOLD);
	private static int roundsPassed = 0, roundsToSelectNewMutator = 6;
	private static int scoreToWin = 50;

	public static void init() {
		Bukkit.getPluginManager().registerEvents(new MoveOrDie(), Plugin.INSTANCE);
		Bukkit.getPluginManager().registerEvents(new PlayerHandler(), Plugin.INSTANCE);
		Bukkit.getPluginManager().registerEvents(new ModeManager(), Plugin.INSTANCE);
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
			Broadcaster.everybody().title(ChatColor.RED + "" + ChatColor.BOLD + "Игра" + ChatColor.BLUE + " Начинается", "", 10, 40, 10)
					.sound(Sound.BLOCK_NOTE_BLOCK_BELL, 1F, 1.5F);
			WorldManager.makeWorld();
			WorldManager.getCurrentGameWorld().setPVP(false);

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
				player.setInvulnerable(true);

				LobbyParkourHandler.stopPassing(player);
			}
			PlayerHandler.getPlayers().forEach(ScoreboardHandler::createGameScoreboard);
			//TODO Now the game starts right from mutator selection
			if(GameSetupManager.FAST_START != 2) GameSetupManager.FAST_START = 1;
			GameSetupManager.start();
			ScoreboardHandler.updateGameTeams();
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
			MapManager.cleanup();
			PlayerHandler.getMDPlayers().clear();
			GameState.STOPPED.set();
			LobbySignManager.updateSigns();
			ScoreboardHandler.updateScoreboardTeams();
			ModeManager.cleanup();
			MutatorSelector.cleanup();
			WorldManager.deleteWorld();
		}
	}

	public static void update() {
		LobbyParkourHandler.update();
		CustomBlockManager.update();
		if(GameState.SETUP.isRunning()) {
			GameSetupManager.update();
		}
		if(GameState.MUTATOR.isRunning()) {
			MutatorSelector.update();
		}
		if(GameState.ROUND_START.isRunning()) {
			if(TaskManager.isSecUpdated()) {
				if(GameState.updateTimer()) {
					ModeManager.beginRound();
				} else {
					if(GameState.getTimer() < 3) {
						for(Player player : PlayerHandler.getPlayers()) {
							ChatColor color = (GameState.getTimer() == 2 ? ChatColor.RED : (GameState.getTimer() == 1 ? ChatColor.YELLOW : ChatColor.BLUE));
							player.sendTitle(color + "" + ChatColor.BOLD + (GameState.getTimer() + 1), "", 0, 20, 1);
							player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1,
									GameState.getTimer() == 2 ? 1 : (GameState.getTimer() == 1 ? 0.75F : 0.5F));
						}
					}
				}
			}
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
					ModeManager.getActiveMode().onRoundEnd();
					if(roundsPassed % roundsToSelectNewMutator == 0 || MutatorManager.FORCE_SELECTOR) {
						MutatorManager.deactivateMutator();
						MutatorSelector.start();
						MutatorManager.FORCE_SELECTOR = false;
					} else {
						if(MutatorManager.getActiveMutator() != null) {
							MutatorManager.getActiveMutator().onRoundEnd();
						}
						ModeManager.startNewRound();
					}
				}
			}
		}
		if(GameState.FINALE.isRunning()) {
			GameFinaleManager.update();
		}
		if(GameState.isPlaying()) {
			PlayerHandler.update();
		}
	}

	public static int getPassedRounds() {
		return roundsPassed;
	}

	public static void increaseRounds() {
		roundsPassed++;
	}

	public static void cycleScoreToWin() {
		scoreToWin += 25;
		if(scoreToWin > 100) {
			scoreToWin = 25;
		}
	}

	public static int getScoreToWin() {
		return scoreToWin;
	}

	public static int getScoreForPlace(int place) {
		if(place == 1) return 5;
		if(place == 2) return 2;
		return 1;
	}

	public static Set<ChatColor> getAvailableColors() {
		return availableColors;
	}

	public static String getLogo() {
		return ChatColor.RED + "" + ChatColor.BOLD + "Move" + ChatColor.RESET + ChatColor.YELLOW + " or " + ChatColor.RESET + ChatColor.BLUE + ChatColor.BOLD
				+ "Die";
	}

	@EventHandler
	public void piston(BlockPistonExtendEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void piston(BlockPistonRetractEvent e) {
		e.setCancelled(true);
	}

}
