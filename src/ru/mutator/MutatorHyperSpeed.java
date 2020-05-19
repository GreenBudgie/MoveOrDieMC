package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.game.PlayerHandler;

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

	@Override
	public void onRoundPrepare() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.removePotionEffect(PotionEffectType.SPEED);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
		}
	}

	@Override
	public void onDeactivate() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.removePotionEffect(PotionEffectType.SPEED);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
		}
	}

}
