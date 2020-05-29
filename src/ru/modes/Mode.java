package ru.modes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.map.GameMap;
import ru.map.MapManager;
import ru.start.Plugin;

public abstract class Mode {

	protected GameMap map;

	public Mode() {
		ModeManager.getModes().add(this);
	}

	public abstract String getName();
	public abstract String getDescription();
	public abstract Material getItemToShow();
	public abstract String getID();
	public abstract boolean allowPVP();
	public abstract boolean allowBlockBreaking();
	public abstract boolean allowBlockPlacing();
	/**
	 * Determines whether to start sudden death after the round ends
	 * @return Whether to allow sudden death
	 */
	public abstract boolean allowSuddenDeath();
	/**
	 * Whether to use point system in this mode Ex: in FIGHT it must count the number of kills by player. If 2 or more players survived it must give the winner
	 * status to a player with the most points
	 * @return Whether to use point system
	 */
	public abstract boolean usePoints();
	public abstract boolean useSurvivalGameMode();
	public abstract int getTime(); //-1 disables the timer

	public final GameMap getMap() {
		return map;
	}

	public final boolean hasTime() {
		return getTime() > 0;
	}

	public void update() {
	}

	public final void prepare() {
		WorldManager.getCurrentGameWorld().setPVP(false);
		map = MapManager.useMapForMode(this);
		map.spreadPlayers();
		for(Player player : PlayerHandler.getPlayers()) {
			PlayerHandler.resetNoEffects(player);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1F, 2F);
			player.sendTitle(getColoredName(), "", 10, 35, 15);
			player.sendMessage(ChatColor.GRAY + "----- " + getColoredName() + ChatColor.RESET + ChatColor.GRAY + " -----");
			player.sendMessage("");
			player.sendMessage(ChatColor.GREEN + getDescription());
			player.sendMessage("");
			player.sendMessage(ChatColor.GRAY + StringUtils.repeat("-", 12 + getName().length()));
			player.setGameMode(useSurvivalGameMode() ? GameMode.SURVIVAL : GameMode.ADVENTURE);
		}
		onRoundPrepare();
	}

	public final void start() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.setInvulnerable(false);
		}
		WorldManager.getCurrentGameWorld().setPVP(allowPVP());
		if(this instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) this, Plugin.INSTANCE);
		}
		onRoundStart();
	}

	public final void end() {
		if(GameState.GAME.isRunning()) {
			for(Player player : PlayerHandler.getPlayers()) {
				player.setInvulnerable(true);
			}
			WorldManager.getCurrentGameWorld().setPVP(false);
			if(this instanceof Listener) {
				HandlerList.unregisterAll((Listener) this);
			}
			onRoundPreEnd();
		}
	}

	public void onRoundPrepare() {
	}

	public void onRoundStart() {
	}

	public void onRoundPreEnd() {
	}

	public void onRoundEnd() {
	}

	public void onPlayerDeath(MDPlayer mdPlayer) {
	}

	public void onPlayerLeave(MDPlayer mdPlayer) {
		if(GameState.GAME.isRunning() && !mdPlayer.isGhost()) {
			mdPlayer.getPlayer().setHealth(0);
		}
	}

	public final boolean isActive() {
		return ModeManager.getActiveMode() == this;
	}

	public final String getColoredName() {
		return ChatColor.DARK_GREEN + "" + ChatColor.BOLD + getName() + ChatColor.RESET;
	}

}
