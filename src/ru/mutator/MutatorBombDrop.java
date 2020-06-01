package ru.mutator;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class MutatorBombDrop extends Mutator {

	private Map<Player, Integer> delay = new HashMap<>();

	@Override
	public Material getItemToShow() {
		return Material.TNT;
	}

	@Override
	public String getName() {
		return "Бомбардировка";
	}

	@Override
	public String getDescription() {
		return "Игроки бросают под себя динамит";
	}

	@Override
	public void update() {
		if(GameState.GAME.isRunning()) {
			for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
				if(!mdPlayer.isGhost()) {
					Player player = mdPlayer.getPlayer();
					if(delay.containsKey(player)) {
						int del = delay.get(player);
						if(del <= 0) {
							TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation(), EntityType.PRIMED_TNT);
							tnt.setFuseTicks(35);
							tnt.setVelocity(new Vector(MathUtils.randomRangeDouble(-0.1, 0.1), MathUtils.randomRangeDouble(0.3, 0.5),
									MathUtils.randomRangeDouble(-0.1, 0.1)));
							delay.put(player, getRandomDelay());
						} else {
							delay.put(player, del - 1);
						}
					}
				}
			}
		}
	}

	private int getRandomDelay() {
		return MathUtils.randomRange(5 * 20, 9 * 20);
	}

	@Override
	public void onRoundStart() {
		for(Player player : PlayerHandler.getPlayers()) {
			delay.put(player, getRandomDelay());
		}
	}

	@Override
	public void onRoundPreEnd() {
		delay.clear();
	}

}
