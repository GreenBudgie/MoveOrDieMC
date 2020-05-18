package ru.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.util.TaskManager;

import java.util.Set;

public class ScoreboardHandler {

	public static Scoreboard lobbyScoreboard;

	public static void createGameScoreboard(Player player) {
		Scoreboard gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Team lobbyTeam = gameScoreboard.registerNewTeam("LobbyTeam");
		lobbyTeam.setColor(ChatColor.GRAY);
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			ChatColor color = mdPlayer.getColor();
			Team team = gameScoreboard.registerNewTeam(color.name() + "Team");
			team.setColor(color);
			team.setCanSeeFriendlyInvisibles(true);
			team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		}
		player.setScoreboard(gameScoreboard);
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

	public static void updateGameScoreboard(Player player) {
		MDPlayer dqPlayer = MDPlayer.fromPlayer(player);
		boolean hasDQ = dqPlayer != null;
		Scoreboard board = player.getScoreboard();
		Objective gameInfo = board.getObjective("gameInfo");
		if(gameInfo != null) gameInfo.unregister();
		gameInfo = board.registerNewObjective("gameInfo", "dummy", MoveOrDie.getLogo());
		gameInfo.setDisplaySlot(DisplaySlot.SIDEBAR);
		int c = 0;

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
