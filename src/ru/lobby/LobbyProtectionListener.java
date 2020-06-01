package ru.lobby;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import ru.game.PlayerHandler;
import ru.game.WorldManager;

public class LobbyProtectionListener implements Listener {

	private boolean isInLobby(Player p) {
		return PlayerHandler.isInLobby(p);
	}

	@EventHandler
	public void damage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(isInLobby(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void noLecternBookTake(PlayerTakeLecternBookEvent e) {
		Player player = e.getPlayer();
		if(isInLobby(player)) {
			if(!player.getInventory().contains(Material.WRITTEN_BOOK)) {
				player.getInventory().addItem(e.getBook());
				player.closeInventory();
			} else {
				player.getInventory().remove(Material.WRITTEN_BOOK);
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void noFarmlandGrief(PlayerInteractEvent e) {
		if(e.getAction() == Action.PHYSICAL && isInLobby(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void noMinecartCollide(VehicleEntityCollisionEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(p.getGameMode() != GameMode.CREATIVE && isInLobby(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void noMinecartDamage(VehicleDamageEvent e) {
		if(e.getAttacker() instanceof Player) {
			Player p = (Player) e.getAttacker();
			if(p.getGameMode() != GameMode.CREATIVE && isInLobby(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void noHangingGrief(HangingBreakByEntityEvent e) {
		if(e.getRemover() instanceof Player) {
			Player p = (Player) e.getRemover();
			if(p.getGameMode() != GameMode.CREATIVE && isInLobby(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void drop(PlayerDropItemEvent e) {
		if(isInLobby(e.getPlayer()) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void move(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(isInLobby(p) && !LobbyParkourHandler.isPassing(p)) {
			if(e.getTo().getY() <= 0) {
				p.teleport(WorldManager.getLobby().getSpawnLocation());
			}
		}
	}

	@EventHandler
	public void noFrameDamage(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player && e.getEntity() instanceof ItemFrame) {
			Player p = (Player) e.getDamager();
			if(isInLobby(p) && p.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void noBlockPlace(BlockPlaceEvent e) {
		if(isInLobby(e.getPlayer()) && e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void noBlockBreak(BlockBreakEvent e) {
		if(isInLobby(e.getPlayer()) && e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			e.setCancelled(true);
		}
	}

}
