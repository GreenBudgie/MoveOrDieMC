package ru.modes;

import org.bukkit.Material;

public abstract class Mode {

	public abstract String getName();
	public abstract String getDescription();
	public abstract Material getItemToShow();
	public abstract String getID();

	public void update() {
	}

}
