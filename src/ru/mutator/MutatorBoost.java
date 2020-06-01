package ru.mutator;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.MathUtils;
import ru.util.ParticleUtils;

import java.util.*;

public class MutatorBoost extends Mutator implements Listener {

	private Set<Player> boostedPlayers = new HashSet<>();

	@Override
	public Material getItemToShow() {
		return Material.FIRE_CHARGE;
	}

	@Override
	public String getName() {
		return "Буст";
	}

	@Override
	public String getDescription() {
		return "При двойном нажатии Space ты получаешь буст вперед";
	}

	@Override
	public void update() {
		Iterator<Player> iterator = boostedPlayers.iterator();
		while(iterator.hasNext()) {
			Player player = iterator.next();
			ParticleUtils.createParticlesAround(player, Particle.SMOKE_LARGE, null, 1);
			if(player.isOnGround()) {
				player.setAllowFlight(true);
				iterator.remove();
			} else {
				player.setAllowFlight(false);
			}
		}
	}

	@Override
	public void onRoundStart() {
		for(Player player : PlayerHandler.getPlayers()) {
			player.setAllowFlight(true);
		}
	}

	@Override
	public void onRoundPreEnd() {
		boostedPlayers.clear();
		for(Player player : PlayerHandler.getPlayers()) {
			player.setAllowFlight(false);
		}
	}

	@Override
	public void onPlayerDeath(MDPlayer player) {
		boostedPlayers.remove(player.getPlayer());
		player.getPlayer().setAllowFlight(false);
	}

	@EventHandler
	public void boost(PlayerToggleFlightEvent e) {
		Player player = e.getPlayer();
		if(GameState.GAME.isRunning() && PlayerHandler.isPlaying(player)) {
			if(!boostedPlayers.contains(player)) {
				Vector direction = player.getLocation().getDirection();
				direction.setY(direction.getY() + 0.1);
				player.setVelocity(direction.multiply(1.3));
				player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1F, 1.2F);
				ParticleUtils.createParticlesAround(player, Particle.FLAME, null, 8);
				boostedPlayers.add(player);
			}
			e.setCancelled(true);
		}
	}

}
