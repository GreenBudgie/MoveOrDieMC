package ru.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.lobby.LobbyEntertainmentHandler;
import ru.lobby.LobbyParkourHandler;
import ru.lobby.sign.LobbySignManager;
import ru.map.MapManager;
import ru.map.MapSetup;
import ru.modes.Mode;
import ru.modes.ModeManager;
import ru.mutator.MutatorManager;
import ru.mutator.MutatorSelector;
import ru.start.Plugin;
import ru.util.Broadcaster;
import ru.util.EntityUtils;
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
			Broadcaster.everybody().title(ChatColor.RED + "" + ChatColor.BOLD + "Игра" + ChatColor.BLUE + " Начинается", "", 10, 60, 20);
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
				PlayerHandler.giveDefaultEffects(player);
				player.setGameMode(GameMode.ADVENTURE);
				ScoreboardHandler.createGameScoreboard(player);
				//Teleporting
				EntityUtils.teleportCentered(player, MapSetup.getSpawn(color), true, true);
			}
			GameState.SETUP.set();
			GameState.setTimer(20);
			ModeManager.setup();
			LobbySignManager.updateSigns();
		}
	}

	public static void endGame() {
		if(GameState.isPlaying()) {
			for(Player player : PlayerHandler.getPlayers()) {
				player.setGameMode(GameMode.SURVIVAL);
				PlayerHandler.reset(player);
				player.teleport(WorldManager.getLobby().getSpawnLocation());
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
			for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
				mdPlayer.update();
			}
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
	}

	public static Set<ChatColor> getAvailableColors() {
		return availableColors;
	}

	public static String getLogo() {
		return ChatColor.RED + "" + ChatColor.BOLD + "Move" + ChatColor.RESET + ChatColor.YELLOW + " or " + ChatColor.RESET + ChatColor.BLUE
				+ ChatColor.BOLD + "Die";
	}

}
