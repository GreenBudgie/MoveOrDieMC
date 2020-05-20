package ru.modes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.map.GameMap;
import ru.map.MapManager;

public abstract class Mode {

	public Mode() {
		ModeManager.getModes().add(this);
	}

	public abstract String getName();
	public abstract String getDescription();
	public abstract Material getItemToShow();
	public abstract String getID();

	public int getTime() {
		return -1; //-1 disables the timer
	}

	public final boolean hasTime() {
		return getTime() > 0;
	}

	public void update() {
	}

	public final void prepare() {
		GameMap map = MapManager.useMapForMode(this);
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
		}
		onRoundPrepare();
	}

	public final void start() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.setInvulnerable(false);
		}
		WorldManager.getCurrentGameWorld().setPVP(allowPVP());
		onRoundStart();
	}

	public final void end() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.setInvulnerable(true);
		}
		WorldManager.getCurrentGameWorld().setPVP(false);
		onRoundPreEnd();
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

	public boolean allowPVP() {
		return false;
	}

	public final boolean isActive() {
		return ModeManager.getActiveMode() == this;
	}

	public final String getColoredName() {
		return ChatColor.DARK_GREEN + "" + ChatColor.BOLD + getName() + ChatColor.RESET;
	}

}
