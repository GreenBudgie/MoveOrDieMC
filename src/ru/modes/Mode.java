package ru.modes;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class Mode {

	public Mode() {
		ModeManager.modes.add(this);
	}

	public abstract String getName();
	public abstract String getDescription();
	public abstract Material getItemToShow();
	public abstract String getID();

	public void update() {
	}

	public void onStart() {
	}

	public void onFinish() {
	}

	public void onPlayerDeath(Player player) {
	}

	public boolean allowPVP() {
		return false;
	}

}
