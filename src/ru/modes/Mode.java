package ru.modes;

import org.bukkit.Material;
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

	public void update() {
	}

	public final void start() {
		GameMap map = MapManager.getRandomMapForMode(this);
		map.spreadPlayers();
		for(Player player : PlayerHandler.getPlayers()) {
			PlayerHandler.resetNoEffects(player);
		}
		WorldManager.getCurrentGameWorld().setPVP(allowPVP());
		onStart();
	}

	public final void end() {
		WorldManager.getCurrentGameWorld().setPVP(false);
		onFinish();
	}

	public void onStart() {
	}

	public void onFinish() {
	}

	public void onPlayerDeath(MDPlayer mdPlayer) {
	}

	public boolean allowPVP() {
		return false;
	}

	public final boolean isActive() {
		return ModeManager.getActiveMode() == this;
	}
}
