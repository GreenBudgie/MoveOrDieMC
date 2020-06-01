package ru.mutator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ru.game.MDPlayer;
import ru.start.Plugin;

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
		if(this instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) this, Plugin.INSTANCE);
		}
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

	public void onPlayerDeath(MDPlayer player) {
	}

	public void onPlayerLeave(MDPlayer player) {
	}

	public void update() {
	}

	public final String getColoredName() {
		return ChatColor.DARK_RED + "" + ChatColor.BOLD + getName() + ChatColor.RESET;
	}

}
