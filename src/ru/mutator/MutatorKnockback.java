package ru.mutator;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.modes.ModeManager;
import ru.util.ParticleUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MutatorKnockback extends Mutator implements Listener {

	@Override
	public Material getItemToShow() {
		return Material.STICK;
	}

	@Override
	public String getName() {
		return "Отдача";
	}

	@Override
	public String getDescription() {
		return "Ты можешь бить игроков, отталкивая их, но урон им наноситься не будет";
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void punch(EntityDamageByEntityEvent e) {
		if(!ModeManager.FIGHT.isActive() && !e.isCancelled()) {
			if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
				MDPlayer damager = MDPlayer.fromPlayer((Player) e.getDamager());
				MDPlayer victim = MDPlayer.fromPlayer((Player) e.getEntity());
				if(damager != null && victim != null) {
					e.setDamage(0);
				}
			}
		}
	}

}
