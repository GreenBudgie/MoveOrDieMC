package ru.lobby.sign;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.game.PlayerHandler;
import ru.start.Plugin;
import ru.util.WorldUtils;

import java.util.ArrayList;
import java.util.List;

public class LobbySignManager implements Listener {

	private static List<LobbySign> signs = new ArrayList<>();

	public static void init() {
		Bukkit.getPluginManager().registerEvents(new LobbySignManager(), Plugin.INSTANCE);
		SignStart start = new SignStart(7, 11, -4);
		SignPoints points = new SignPoints(6, 11, -4);
		SignHP hp = new SignHP(7, 12, -4);
		SignRating rating = new SignRating(8, 11, -4);
		signs.addAll(Lists.newArrayList(start, points, hp, rating));
		updateSigns();
	}

	public static LobbySign getSignAt(Location l) {
		for(LobbySign sign : signs) {
			if(WorldUtils.compareIntegerLocations(l, sign.getLocation())) return sign;
		}
		return null;
	}

	public static void updateSigns() {
		signs.forEach(LobbySign::updateText);
	}

	@EventHandler
	public void signClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && PlayerHandler.isInLobby(p)) {
			Block block = e.getClickedBlock();
			LobbySign sign = getSignAt(block.getLocation());
			if(sign != null) {
				sign.onClick(p);
				updateSigns();
			}
		}
	}

}
