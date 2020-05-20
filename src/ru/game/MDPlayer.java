package ru.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.modes.ModeManager;
import ru.util.MathUtils;
import ru.util.ParticleUtils;
import ru.util.TaskManager;

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

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void addScore(int score) {
		this.score += score;
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
		if(TaskManager.isSecUpdated()) {
			ScoreboardHandler.updateGameScoreboard(player);
		}
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
			ParticleUtils.createParticlesInsideSphere(player.getLocation(), 3, Particle.FALLING_LAVA, null, 25);
			ParticleUtils.createParticlesInsideSphere(player.getLocation(), 2, Particle.REDSTONE, ParticleUtils.toColor(color), 40);
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_HONEY_BLOCK_FALL, 1.5F, 0.5F);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.5F, 1.5F);
			PlayerHandler.reset(player);
			PlayerHandler.giveGhostEffects(player);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, false));
			PlayerHandler.setDeathHandle(this);
			ScoreboardHandler.updateGameTeams();
		}
	}

	public void resurrect() {
		if(isGhost) {
			isGhost = false;
			player.setInvulnerable(true);
			PlayerHandler.reset(player);
			PlayerHandler.givePlayerEffects(player);
			ScoreboardHandler.updateGameTeams();
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
			ModeManager.getActiveMode().onPlayerDeath(this);
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
