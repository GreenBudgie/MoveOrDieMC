package ru.game;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.ScoreboardManager;
import ru.modes.ModeManager;
import ru.mutator.MutatorManager;
import ru.mutator.MutatorSelector;
import ru.util.*;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents an in-game player, dead or alive
 */
public class MDPlayer {

	private final int maxMoveHP = 160;
	private String nickname;
	private Player player;
	private ChatColor color;
	private boolean isGhost = false;
	private BossBar moveBar;
	private int moveHP = maxMoveHP;
	private int score = 0; //Overall score
	private int points = 0; //Current round points
	private boolean left = false;
	private int sidebarSize = 3; //3 - all information, 2 - reduced info (no nicknames, only colors; no additional info), 1 - only primary info (alive colors, mode time); 0 - hide info

	public MDPlayer(Player player) {
		this.player = player;
		this.nickname = player.getName();
		moveBar = Bukkit.createBossBar(MoveOrDie.getLogo(), BarColor.GREEN, BarStyle.SOLID);
		if(PlayerHandler.getHPDisplay(player) != PlayerHandler.HPDisplay.ACTIONBAR) {
			moveBar.setProgress(1);
			moveBar.addPlayer(player);
			moveBar.setVisible(true);
		}
	}

	public int getSidebarSize() {
		return sidebarSize;
	}

	public void switchSidebarSize() {
		sidebarSize = sidebarSize == 0 ? 3 : sidebarSize - 1;
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F,  (sidebarSize + 1) * 0.1F + 1.2F);
		ScoreboardHandler.updateGameScoreboard(player);
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

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getPoints() {
		return points;
	}

	public void addPoint() {
		if(!isGhost) points++;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void resetPoints() {
		points = 0;
	}

	public void addScore(int score, boolean canWin) {
		this.score = Math.min(this.score + score, canWin ? MoveOrDie.getScoreToWin() : MoveOrDie.getScoreToWin() - 1);
	}

	public void addScore(int score) {
		addScore(score, false);
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
		if(!ModeManager.isSuddenDeath() && !MutatorManager.FLY_OR_DIE.isActive()) {
			changeMoveHp(8);
		}
	}

	public void handleWalkHp() {
		if(!ModeManager.isSuddenDeath() && !MutatorManager.FLY_OR_DIE.isActive()) {
			changeMoveHp(5);
		}
	}

	public void changeMoveHp(int amount) {
		if(MutatorManager.HARDCORE.isActive()) amount *= 2;
		if(ModeManager.BOMB_TAG.isActive()) {
			if(ModeManager.BOMB_TAG.getTaggedPlayer() == player) {
				if(amount < 0) amount /= 2;
			}
		}
		moveHP += amount;
	}

	private void updateActionBar() {
		if(PlayerHandler.getHPDisplay(player) == PlayerHandler.HPDisplay.BOSSBAR) return;
		String symbol = "\u2764";
		final int toShow = 20;
		if(GameState.isInGame()) {
			if(isGhost) {
				String result = ChatColor.DARK_RED + "" + ChatColor.BOLD + "\u274C " + ChatColor.RESET + ChatColor.GRAY + StringUtils.repeat(symbol, toShow)
						+ ChatColor.DARK_RED + ChatColor.BOLD + " \u274C";
				EntityUtils.sendActionBarInfo(player, result);
			} else {
				double value = (moveHP / (double) maxMoveHP);
				int alive = (int) Math.round(value * toShow);
				int dead = toShow - alive;
				int arrowCount = 1;
				ChatColor color, arrowsColor;
				if(value > 0.5) {
					color = ChatColor.GREEN;
					arrowsColor = ChatColor.GRAY;
				} else if(value > 0.25) {
					arrowCount = (int) Math.round(5 - value * 8);
					color = ChatColor.YELLOW;
					arrowsColor = ChatColor.RED;
				} else {
					arrowCount = (int) Math.round(7 - value * 16);
					color = ChatColor.RED;
					arrowsColor = ChatColor.DARK_RED;
				}
				if(ModeManager.isSuddenDeath()) {
					arrowsColor = ChatColor.DARK_RED;
				}
				String result = arrowsColor + "" + ChatColor.BOLD + StringUtils.repeat("<", arrowCount) + " " + ChatColor.RESET;
				result += color + StringUtils.repeat(symbol, alive);
				result += ChatColor.DARK_GRAY + StringUtils.repeat(symbol, dead) + arrowsColor + ChatColor.BOLD + " " + StringUtils.repeat(">", arrowCount);
				EntityUtils.sendActionBarInfo(player, result);
			}
		}
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
				boolean closeToGhost = false;
				for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
					if(mdPlayer != this && mdPlayer.isGhost) {
						double distSq = mdPlayer.player.getLocation().distanceSquared(player.getLocation());
						double ghostRadius = 1.5;
						if(distSq < ghostRadius * ghostRadius) {
							closeToGhost = true;
							break;
						}
					}
				}
				float walkSpeed = 0.2F;
				if(closeToGhost) {
					if(MutatorManager.DEATH_TOUCH.isActive() && MoveOrDie.DO_MOVE_DAMAGE) {
						moveHP -= 10;
						walkSpeed = 0.15F;
					} else {
						walkSpeed = 0.08F;
						player.removePotionEffect(PotionEffectType.JUMP);
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false));
					}
				} else {
					if(!MutatorManager.JUMP_BOOST.isActive()) {
						player.removePotionEffect(PotionEffectType.JUMP);
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false));
					}
				}
				if(ModeManager.BOMB_TAG.isActive() && ModeManager.BOMB_TAG.getTaggedPlayer() == player) {
					walkSpeed += 0.1F;
				}
				player.setWalkSpeed(walkSpeed);
				if(moveHP > 0) {
					if(MoveOrDie.DO_MOVE_DAMAGE) {
						if(!ModeManager.isSuddenDeath()) {
							if(MutatorManager.FLY_OR_DIE.isActive()) {
								if(!player.isOnGround()) {
									changeMoveHp(6);
								}
							}
							if(player.isOnGround()) {
								changeMoveHp(-4);
							} else {
								if(!MutatorManager.FLY_OR_DIE.isActive()) {
									changeMoveHp(-2);
								}
							}
						} else {
							changeMoveHp(-1);
						}
					}
				} else {
					player.setHealth(0);
				}
			} else {
				moveHP = maxMoveHP;
			}
			moveHP = MathUtils.clamp(moveHP, 0, maxMoveHP);
			double value = (double) moveHP / maxMoveHP;
			if(value > 0.5) {
				moveBar.setColor(BarColor.GREEN);
			} else if(value > 0.25) {
				moveBar.setColor(BarColor.YELLOW);
			} else {
				moveBar.setColor(BarColor.RED);
			}
			moveBar.setProgress(value);
			if(value < 0.25) {
				player.sendTitle("", ChatColor.DARK_RED + "" + ChatColor.BOLD + "Опасно!", 0, 3, 5);
			}
		}
		updateActionBar();
	}

	/**
	 * Turns player to a ghost, i.g. kills him
	 */
	public void makeGhost() {
		if(!isGhost) {
			isGhost = true;
			player.setCollidable(false);
			player.setInvulnerable(true);
			ParticleUtils.createParticlesInsideSphere(player.getLocation(), 3, Particle.FALLING_LAVA, null, 25);
			ParticleUtils.createParticlesInsideSphere(player.getLocation(), 2, Particle.REDSTONE, ParticleUtils.toColor(color), 40);
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_HONEY_BLOCK_FALL, 1.5F, 0.5F);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.7F, 1.5F);
			PlayerHandler.reset(player);
			PlayerHandler.giveGhostEffects(player);
			player.setGameMode(GameMode.ADVENTURE);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, false));
			PlayerHandler.setDeathHandle(this);
			ScoreboardHandler.updateGameTeams();
		}
	}

	public boolean isLeft() {
		return left;
	}

	public void resurrect() {
		if(isGhost) {
			isGhost = false;
			player.setCollidable(true);
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
		left = true;
		if(GameState.MUTATOR.isRunning()) {
			MutatorSelector.handlePlayerLeave(this);
		}
		if(GameState.isInGame()) {
			if(ModeManager.getActiveMode() != null) {
				ModeManager.getActiveMode().onPlayerLeave(this);
			}
			if(MutatorManager.getActiveMutator() != null) {
				MutatorManager.getActiveMutator().onPlayerLeave(this);
			}
		}
		if(GameState.GAME.isRunning()) {
			Iterator<Set<MDPlayer>> iterator = PlayerHandler.getDeathQueue().iterator();
			while(iterator.hasNext()) {
				Set<MDPlayer> set = iterator.next();
				set.remove(this);
				if(set.isEmpty()) {
					iterator.remove();
				}
			}
		}
		Broadcaster.each(PlayerHandler.getPlayers()).toChat(color + nickname + ChatColor.RED + ChatColor.BOLD + " вышел из игры");
		player.teleport(WorldManager.getLobby().getSpawnLocation());
		player.setGameMode(GameMode.SURVIVAL);
		cleanup();
		remove();
		if(PlayerHandler.getMDPlayers().size() == 1) {
			if(ModeManager.getActiveMode() != null) {
				ModeManager.getActiveMode().end();
				ModeManager.getActiveMode().onRoundEnd();
			}
			GameFinaleManager.start(PlayerHandler.getMDPlayers().get(0));
		}
		if(PlayerHandler.getMDPlayers().isEmpty()) {
			MoveOrDie.endGame();
		}
	}

	public void onDeath() {
		if(GameState.GAME.isRunning()) {
			Broadcaster.each(PlayerHandler.getPlayers()).toChat(ChatColor.DARK_RED + "" + ChatColor.BOLD + "\u274C " + ChatColor.RESET + color + nickname);
			ModeManager.getActiveMode().onPlayerDeath(this);
			if(MutatorManager.getActiveMutator() != null) {
				MutatorManager.getActiveMutator().onPlayerDeath(this);
			}
			makeGhost();
		}
	}

	public String toString() {
		return color + nickname + "; score=" + score + "; points=" + points;
	}

}
