package ru.game;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ru.util.EntityUtils;

import javax.annotation.Nullable;

/**
 * Represents an in-game player, dead or alive
 */
public class MDPlayer {

	private String nickname;
	private Player player;
	private ChatColor color;
	private boolean isGhost;
	private BossBar moveBar;
	private final int maxMoveHP = 80;
	private int moveHP = maxMoveHP;

	public MDPlayer(Player player) {
		this.player = player;
		this.nickname = player.getName();
	}

	public String getNickname() {
		return nickname;
	}

	public Player getPlayer() {
		return player;
	}

	public ChatColor getColor() {
		return color;
	}

	public void setColor(ChatColor color) {
		this.color = color;
	}

	public boolean isGhost() {
		return isGhost;
	}

	public void reset() {
		PlayerHandler.reset(player);
	}

	public void update() {
		ScoreboardHandler.updateGameScoreboard(player);
	}

	/**
	 * Turns player to a ghost, i.g. kills him
	 */
	public void makeGhost() {
		if(!isGhost) {

		}
	}

	public void resurrect() {
		if(isGhost) {

		}
	}

	public void remove() {
		PlayerHandler.getMDPlayers().remove(this);
	}

	public void onLeave() {
		player.teleport(WorldManager.getLobby().getSpawnLocation());
		remove();
	}

	public void onDeath() {
		isGhost = true;
	}

	/**
	 * Gets the MDPlayer from a specific player
	 * @param player The player
	 * @return An MDPlayer, or null if not present
	 */
	@Nullable
	public static MDPlayer fromPlayer(Player player) {
		for(MDPlayer mdplayer : PlayerHandler.getMDPlayers()) {
			if(player.getName().equals(mdplayer.nickname)) return mdplayer;
		}
		return null;
	}

}
