package ru.mutator;

import org.bukkit.GameRule;
import org.bukkit.Material;
import ru.game.WorldManager;

public class MutatorFallDamage extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.LEATHER_BOOTS;
	}

	@Override
	public String getName() {
		return "Жесткое Падение";
	}

	@Override
	public String getDescription() {
		return "Теперь наносится урон от падения";
	}

	@Override
	public void onRoundStart() {
		WorldManager.getCurrentGameWorld().setGameRule(GameRule.FALL_DAMAGE, true);
	}

	@Override
	public void onRoundPreEnd() {
		WorldManager.getCurrentGameWorld().setGameRule(GameRule.FALL_DAMAGE, false);
	}

	@Override
	public void onDeactivate() {
		WorldManager.getCurrentGameWorld().setGameRule(GameRule.FALL_DAMAGE, false);
	}

}
