package ru.modes;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.InventoryUtils;
import ru.util.ItemUtils;
import ru.util.ParticleUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ModeStruggle extends Mode implements Listener {

	private Set<Attack> attacks = new HashSet<>();

	private class Attack {

		Player attacker, victim;
		int delayToRemove;

		Attack(Player victim, Player attacker) {
			this.victim = victim;
			this.attacker = attacker;
			resetDelay();
		}

		void resetDelay() {
			delayToRemove = 100;
		}

	}

	@Override
	public String getName() {
		return "Борьба";
	}

	@Override
	public String getDescription() {
		return "Выкидывай игроков за карту";
	}

	@Override
	public Material getItemToShow() {
		return Material.SLIME_BALL;
	}

	@Override
	public String getID() {
		return "STRUGGLE";
	}

	@Override
	public void update() {
		attacks.removeIf(attack -> attack.delayToRemove-- <= 0);
	}

	@Nullable
	private Player getCustomAttacker(Player victim) {
		for(Attack attack : attacks) {
			if(attack.victim == victim) {
				return attack.attacker;
			}
		}
		return null;
	}

	private boolean hasCustomAttack(Player victim, Player attacker) {
		for(Attack attack : attacks) {
			if(attack.victim == victim && attack.attacker == attacker) return true;
		}
		return false;
	}

	private void setCustomAttacker(Player victim, Player attacker)  {
		if(hasCustomAttack(victim, attacker)) {
			for(Attack attack : attacks) {
				if(attack.victim == victim && attack.attacker == attacker) {
					attack.resetDelay();
					return;
				}
			}
		} else {
			attacks.add(new Attack(victim, attacker));
		}
	}

	@Override
	public void onPlayerDeath(MDPlayer mdPlayer) {
		Player player = mdPlayer.getPlayer();
		if(player.getKiller() != null) {
			Player killer = getCustomAttacker(player);
			MDPlayer killerMd = MDPlayer.fromPlayer(killer);
			if(killerMd != null && !killerMd.isGhost()) {
				killerMd.addPoint();
			}
		}
	}

	@EventHandler
	public void damage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player victim = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();
			if(PlayerHandler.isPlayingAndAlive(victim) && PlayerHandler.isPlayingAndAlive(damager)) {
				setCustomAttacker(victim, damager);
				Vector velocity = damager.getLocation().getDirection();
				velocity.setY(Math.max(0, velocity.getY()) + 0.2);
				victim.setVelocity(velocity);
				e.setDamage(0);
			}
		}
	}

	@Override
	public int getTime() {
		return 45;
	}

	@Override
	public boolean allowPVP() {
		return true;
	}

	@Override
	public boolean allowBlockBreaking() {
		return false;
	}

	@Override
	public boolean allowBlockPlacing() {
		return false;
	}

	@Override
	public boolean allowSuddenDeath() {
		return true;
	}

	@Override
	public boolean usePoints() {
		return true;
	}

	@Override
	public boolean useSurvivalGameMode() {
		return false;
	}

}
