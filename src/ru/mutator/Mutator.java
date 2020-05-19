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

	public final boolean isActive() {
		return this == MutatorManager.getActiveMutator();
	}

	public final void setActive() {
		MutatorManager.activeMutator = this;
		onFirstActivate();
	}

	public void onRoundEnd() {
	}

	public void onRoundPreEnd() {
	}

	public void onRoundPrepare() {
	}

	public void onRoundStart() {
	}

	public void onFirstActivate() {
	}

	public void onDeactivate() {
	}

	public void update() {
	}

	public final String getColoredName() {
		return ChatColor.DARK_RED + "" + ChatColor.BOLD + getName() + ChatColor.RESET;
	}

}
