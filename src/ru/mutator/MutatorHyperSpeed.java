package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class MutatorHyperSpeed extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.SUGAR;
	}

	@Override
	public String getName() {
		return "Гиперскорость";
	}

	@Override
	public String getDescription() {
		return "Все становятся максимально быстрыми";
	}

}
