package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.game.PlayerHandler;

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

	@Override
	public void onRoundPrepare() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 5, false, false));
		}
	}

	@Override
	public void onDeactivate() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false));
		}
	}

}
