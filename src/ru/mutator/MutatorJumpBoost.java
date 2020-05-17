package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class MutatorJumpBoost extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.RABBIT_FOOT;
	}

	@Override
	public String getName() {
		return "К Небесам!";
	}

	@Override
	public String getDescription() {
		return "Прыжки становятся дико высокими";
	}

}
