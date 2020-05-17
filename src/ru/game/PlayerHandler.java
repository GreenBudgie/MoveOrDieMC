package ru.game;

import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.util.Broadcaster;
import ru.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerHandler implements Listener {

	private static List<MDPlayer> players = new ArrayList<>();

	public static List<Player> getPlayers() {
		return players.stream().map(MDPlayer::getPlayer).collect(Collectors.toList());
	}

	public static List<MDPlayer> getMDPlayers() {
		return players;
	}

	public static boolean isInLobby(Player p) {
		return WorldManager.getLobby().getPlayers().stream().anyMatch(p::equals);
	}

	public static boolean isPlaying(Player player) {
		return MDPlayer.fromPlayer(player) != null;
	}

	public static void reset(Player player) {
		player.getActivePotionEffects().forEach(ef -> player.removePotionEffect(ef.getType()));
		resetNoEffects(player);
	}

	public static void resetNoEffects(Player player) {
		player.getInventory().clear();
		EntityUtils.heal(player);
		player.setFireTicks(0);
		player.setNoDamageTicks(0);
		player.setExp(0);
		player.setLevel(0);
	}

	public static void giveDefaultEffects(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler
	public void join(PlayerJoinEvent e) {
		e.setJoinMessage(null);
		Player player = e.getPlayer();
		reset(player);
		Broadcaster.inWorld(WorldManager.getLobby()).and(player).toChat(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " присоединился");
		if(player.getWorld() != WorldManager.getLobby()) {
			player.teleport(WorldManager.getLobby().getSpawnLocation());
		}
		ScoreboardHandler.updateScoreboardTeamsLater();
	}

	@EventHandler
	public void leave(PlayerQuitEvent e) {
		e.setQuitMessage(null);
		Player player = e.getPlayer();
		if(isPlaying(player)) {
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null) mdPlayer.onLeave();
		}
		if(isInLobby(player)) {
			Broadcaster.inWorld(WorldManager.getLobby()).toChat(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " отключился");
		}
		ScoreboardHandler.updateScoreboardTeamsLater();
	}

	@EventHandler
	public void noDrop(PlayerDropItemEvent e) {
		if(isPlaying(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void noInventoryInteract(InventoryClickEvent e) {
		if(isPlaying((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

}
