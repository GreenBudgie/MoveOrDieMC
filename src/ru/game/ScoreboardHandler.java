package ru.game;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import ru.modes.ModeManager;
import ru.mutator.MutatorManager;
import ru.util.TaskManager;

import java.util.*;

public class ScoreboardHandler {

	private static Scoreboard lobbyScoreboard;
	private static Map<MDPlayer, Integer> addedScore = new HashMap<>();

	public static void createGameScoreboard(Player player) {
		Scoreboard gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Team lobbyTeam = gameScoreboard.registerNewTeam("LobbyTeam");
		lobbyTeam.setColor(ChatColor.GRAY);
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			ChatColor color = mdPlayer.getColor();
			Team team = gameScoreboard.registerNewTeam(color.name() + "Team");
			team.setColor(color);
			team.setCanSeeFriendlyInvisibles(true);
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
			team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		}
		player.setScoreboard(gameScoreboard);
	}

	public static void updateGameTeamsLater() {
		TaskManager.invokeLater(ScoreboardHandler::updateGameTeams);
	}

	public static void updateGameTeams() {
		if(!GameState.isPlaying()) return;
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			Scoreboard gameScoreboard = mdPlayer.getPlayer().getScoreboard();
			clearEntries(gameScoreboard);
			for(Player currentPlayer : Bukkit.getOnlinePlayers()) {
				if(PlayerHandler.isInLobby(currentPlayer)) {
					gameScoreboard.getTeam("LobbyTeam").addEntry(currentPlayer.getName());
					continue;
				}
				if(PlayerHandler.isPlaying(currentPlayer)) {
					MDPlayer currentMDPlayer = MDPlayer.fromPlayer(currentPlayer);
					if(currentMDPlayer.isGhost()) {
						gameScoreboard.getTeam(mdPlayer.getColor().name() + "Team").addEntry(currentPlayer.getName());
					} else {
						gameScoreboard.getTeam(currentMDPlayer.getColor().name() + "Team").addEntry(currentPlayer.getName());
					}
				}
			}
		}
	}

	public static void setAddedScore(MDPlayer player, int score) {
		addedScore.put(player, score);
	}

	public static void cleanup() {
		addedScore.clear();
	}

	public static void updateGameScoreboard(Player player) {
		MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
		Scoreboard board = player.getScoreboard();
		Objective gameInfo = board.getObjective("gameInfo");
		if(gameInfo != null) gameInfo.unregister();
		gameInfo = board.registerNewObjective("gameInfo", "dummy", MoveOrDie.getLogo());
		gameInfo.setDisplaySlot(DisplaySlot.SIDEBAR);
		int c = 0;
		List<MDPlayer> sortedPlayers = Lists.newArrayList(PlayerHandler.getMDPlayers());
		sortedPlayers.sort(Comparator.comparingInt(MDPlayer::getScore));
		for(MDPlayer currentPlayer : sortedPlayers) {
			String bold = currentPlayer == mdPlayer ? ChatColor.BOLD + "" : "";
			String dead = currentPlayer.isGhost() && GameState.GAME.isRunning() ? ChatColor.STRIKETHROUGH + "" : "";
			String scoreInfo = GameState.ROUND_END.isRunning() ? ChatColor.DARK_GREEN + " +" + addedScore.getOrDefault(currentPlayer, 0) : "";
			Score score = gameInfo.getScore(
					currentPlayer.getColor() + bold + dead + currentPlayer.getNickname() + ChatColor.RESET + ChatColor.GRAY + " - " + ChatColor.GOLD + ChatColor.BOLD
							+ currentPlayer.getScore() + scoreInfo);
			score.setScore(c++);
		}
		Score splitter = gameInfo.getScore(ChatColor.GRAY + " - ");
		splitter.setScore(c++);
		if(GameState.isInGame()) {
			if(MutatorManager.getActiveMutator() != null) {
				Score mutator = gameInfo.getScore(
						ChatColor.YELLOW + "�������" + ChatColor.GRAY + ": " + ChatColor.DARK_RED + ChatColor.BOLD + MutatorManager.getActiveMutator().getName());
				mutator.setScore(c++);
			}
			String timer = ModeManager.getActiveMode().hasTime() && GameState.GAME.isRunning() ? ChatColor.AQUA + " " + GameState.getTimer() : "";
			Score mode = gameInfo.getScore(
					ChatColor.GREEN + "�����" + ChatColor.GRAY + ": " + ModeManager.getActiveMode().getColoredName() + timer);
			mode.setScore(c++);
		}
		Score round = gameInfo.getScore(ChatColor.DARK_GREEN + "�����" + ChatColor.GRAY + ": " + ChatColor.WHITE + ChatColor.BOLD + MoveOrDie.getPassedRounds());
		round.setScore(c);
	}

	public static void createLobbyScoreboard() {
		Scoreboard lobbyScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Team lobbyTeam = lobbyScoreboard.registerNewTeam("LobbyTeam");
		Team gameTeam = lobbyScoreboard.registerNewTeam("GameTeam");
		lobbyTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		lobbyTeam.setCanSeeFriendlyInvisibles(true);
		lobbyTeam.setColor(ChatColor.GOLD);
		gameTeam.setColor(ChatColor.DARK_AQUA);
		ScoreboardHandler.lobbyScoreboard = lobbyScoreboard;
		for(Player player : WorldManager.getLobby().getPlayers()) {
			player.setScoreboard(lobbyScoreboard);
		}
		updateLobbyTeams();
	}

	public static void updateLobbyTeams() {
		if(lobbyScoreboard == null) return;
		Team lobbyTeam = lobbyScoreboard.getTeam("LobbyTeam");
		Team gameTeam = lobbyScoreboard.getTeam("GameTeam");
		clearEntries(lobbyScoreboard);
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(PlayerHandler.isInLobby(player)) {
				lobbyTeam.addEntry(player.getName());
				player.setScoreboard(lobbyScoreboard);
			}
			if(PlayerHandler.isPlaying(player)) {
				gameTeam.addEntry(player.getName());
			}
		}
	}

	public static void updateLobbyTeamsLater() {
		TaskManager.invokeLater(ScoreboardHandler::updateLobbyTeams);
	}

	private static void clearEntries(Scoreboard board) {
		for(Team team : board.getTeams()) {
			Set<String> entries = team.getEntries();
			for(String entry : entries) {
				team.removeEntry(entry);
			}
		}
	}

	public static void updateScoreboardTeams() {
		updateLobbyTeams();
		updateGameTeams();
	}

	public static void updateScoreboardTeamsLater() {
		TaskManager.invokeLater(() -> {
			updateLobbyTeams();
			updateGameTeams();
		});
	}

}
