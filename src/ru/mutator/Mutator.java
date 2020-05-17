package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public abstract class Mutator {

	public Mutator() {
		MutatorManager.getMutators().add(this);
	}

	public abstract Material getItemToShow();
	public abstract String getName();
	public abstract String getDescription();

	public final void activate() {
		onActivate();
	}

	public final boolean isActive() {
		return this == MutatorManager.getActiveMutator();
	}

	public void onDeactivate() {
	}

	public final void deactivate() {
		onDeactivate();
	}

	public void onActivate() {
	}

	public void update() {
	}

	public final String getColoredName() {
		return ChatColor.DARK_RED + "" + ChatColor.BOLD + getName() + ChatColor.RESET;
	}

}
