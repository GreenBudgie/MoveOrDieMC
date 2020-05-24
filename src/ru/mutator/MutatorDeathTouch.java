package ru.mutator;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.ParticleUtils;
import ru.util.TaskManager;

public class MutatorDeathTouch extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.REDSTONE;
	}

	@Override
	public String getName() {
		return "Смертельное Касание";
	}

	@Override
	public String getDescription() {
		return "Рядом стоящие призраки довольно быстро убивают тебя";
	}

	@Override
	public void update() {
		if(TaskManager.ticksPassed(10)) {
			for(MDPlayer player : PlayerHandler.getMDPlayers()) {
				if(player.isGhost()) {
					ParticleUtils.createParticlesAround(player.getPlayer(), Particle.REDSTONE, Color.RED, 4);
				}
			}
		}
	}

}
