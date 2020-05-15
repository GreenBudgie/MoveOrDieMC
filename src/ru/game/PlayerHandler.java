package ru.game;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.util.Broadcaster;
import ru.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayerHandler implements Listener {

	private static List<MDPlayer> players = new ArrayList<>();

	public static List<MDPlayer> getPlayers() {
		return players;
	}

	public static boolean isInLobby(Player p) {
		return WorldManager.getLobby().getPlayers().stream().anyMatch(p::equals);
	}

	public static boolean isPlaying(Player player) {
		return MDPlayer.fromPlayer(player) != null;
	}

	public static void reset(Player player) {
		player.getInventory().clear();
		player.getActivePotionEffects().forEach(ef -> player.removePotionEffect(ef.getType()));
		EntityUtils.heal(player);
		player.setFireTicks(0);
		player.setNoDamageTicks(0);
		player.setExp(0);
		player.setLevel(0);
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
	}

}
