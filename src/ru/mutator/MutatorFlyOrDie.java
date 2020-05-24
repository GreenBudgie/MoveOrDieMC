package ru.mutator;

import org.bukkit.Material;

public class MutatorFlyOrDie extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.PHANTOM_MEMBRANE;
	}

	@Override
	public String getName() {
		return "Лети или Умри";
	}

	@Override
	public String getDescription() {
		return "Здоровье регенерируется только в воздухе";
	}

}
