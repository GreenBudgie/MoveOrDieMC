package ru.mutator;

import org.bukkit.Material;

public abstract class Mutator {

	public Mutator() {
		MutatorManager.mutators.add(this);
	}

	public abstract Material getItemToShow();
	public abstract String getName();
	public abstract String getDescription();

	public final void activate() {
		onActivate();
	}

	public final boolean isActive() {
		return false; //TODO
	}

	public void onDeactivate() {
	}

	public final void deactivate() {
		onDeactivate();
	}

	public final String getInfo() {
		return ""; //TODO
	}

	public void onActivate() {
	}

	public void update() {
	}

}
