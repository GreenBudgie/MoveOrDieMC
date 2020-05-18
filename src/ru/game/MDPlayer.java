package ru.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ru.util.MathUtils;

import javax.annotation.Nullable;

/**
 * Represents an in-game player, dead or alive
 */
public class MDPlayer {

	private String nickname;
	private Player player;
	private ChatColor color;
	private boolean isGhost = false;
	private BossBar moveBar;
	private final int maxMoveHP = 80;
	private int moveHP = maxMoveHP;
	private int score = 0;

	public MDPlayer(Player player) {
		this.player = player;
		this.nickname = player.getName();
		moveBar = Bukkit.createBossBar(MoveOrDie.getLogo(), BarColor.GREEN, BarStyle.SOLID);
		moveBar.setProgress(1);
		moveBar.addPlayer(player);
		moveBar.setVisible(true);
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

	public void handleSprintHp() {
		moveHP = MathUtils.clamp(moveHP + 2, 0, maxMoveHP);
	}

	public void handleWalkHp() {
		moveHP = MathUtils.clamp(moveHP + 1, 0, maxMoveHP);
	}

	public void update() {
		ScoreboardHandler.updateGameScoreboard(player);
		if(isGhost) {
			moveBar.setProgress(0);
			moveBar.setColor(BarColor.WHITE);
			moveHP = maxMoveHP;
		} else {
			if(GameState.GAME.isRunning()) {
				if(moveHP > 0) {
					moveHP -= 1;
				} else {
					player.setHealth(0);
				}
			} else {
				moveHP = maxMoveHP;
			}
			double value = (double) moveHP / maxMoveHP;
			if(value > 0.5) {
				moveBar.setColor(BarColor.GREEN);
			} else if(value > 0.25) {
				moveBar.setColor(BarColor.YELLOW);
			} else {
				moveBar.setColor(BarColor.RED);
			}
			moveBar.setProgress(value);
		}
	}

	/**
	 * Turns player to a ghost, i.g. kills him
	 */
	public void makeGhost() {
		if(!isGhost) {
			isGhost = true;
			player.setInvulnerable(true);
			PlayerHandler.reset(player);
			PlayerHandler.giveGhostEffects(player);
			PlayerHandler.setDeathHandle(this);
		}
	}

	public void resurrect() {
		if(isGhost) {
			isGhost = false;
			player.setInvulnerable(true);
			PlayerHandler.reset(player);
			PlayerHandler.givePlayerEffects(player);
		}
	}

	public void cleanup() {
		moveBar.removeAll();
		moveBar.setVisible(false);
	}

	public void remove() {
		PlayerHandler.getMDPlayers().remove(this);
	}

	public void onLeave() {
		player.teleport(WorldManager.getLobby().getSpawnLocation());
		cleanup();
		remove();
	}

	public void onDeath() {
		if(GameState.GAME.isRunning()) {
			makeGhost();
		}
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
