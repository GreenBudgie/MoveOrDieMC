package ru.lobby;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.start.Plugin;
import ru.util.EntityUtils;
import ru.util.MathUtils;
import ru.util.WorldUtils;

import java.util.*;

public class LobbyParkourHandler implements Listener {

	private static final World lobby = WorldManager.getLobby();
	private static Map<Player, LobbyParkour> passingParkours = new HashMap<>();
	private static Set<Player> toUpdate = new HashSet<>();
	private static Map<Player, Integer> passingTicks = new HashMap<>();
	public static List<LobbyParkour> parkours = new ArrayList<>();

	public static void init() {
		LobbyParkour easy = new LobbyParkour();
		easy.setName(ChatColor.GREEN + "Изи");
		easy.setStartLocation(new Location(lobby, 24, 11, -1));
		easy.setFinishLocation(new Location(lobby, 34, 19, -18));
		easy.setCheckpointLocation(new Location(lobby, 21.5, 10, 2.5, 225, 0));
		easy.setSignLocation(new Location(lobby, 23, 10, 1));

		LobbyParkour medium = new LobbyParkour();
		medium.setName(ChatColor.DARK_GREEN + "Средний");
		medium.setStartLocation(new Location(lobby, -11, 11, -2));
		medium.setFinishLocation(new Location(lobby, -17, 22, -16));
		medium.setCheckpointLocation(new Location(lobby, -6.5, 10, 2.5, 135, 0));
		medium.setSignLocation(new Location(lobby, -9, 10, 1));

		LobbyParkour hard = new LobbyParkour();
		hard.setName(ChatColor.DARK_BLUE + "Сложный");
		hard.setStartLocation(new Location(lobby, 25, 24, -19));
		hard.setFinishLocation(new Location(lobby, 16, 26, -20));
		hard.setCheckpointLocation(new Location(lobby, 25.5, 23, -15.5, 180, 0));
		hard.setSignLocation(new Location(lobby, 25, 23, -18));

		LobbyParkour insane = new LobbyParkour();
		insane.setName(ChatColor.DARK_RED + "Дикий");
		insane.setStartLocation(new Location(lobby, -12, 24, -19));
		insane.setFinishLocation(new Location(lobby, -2, 26, -20));
		insane.setCheckpointLocation(new Location(lobby, -11.5, 23, -15.5, 180, 0));
		insane.setSignLocation(new Location(lobby, -12, 23, -18));

		LobbyParkour ice = new LobbyParkour();
		ice.setName(ChatColor.AQUA + "Ледовый");
		ice.setStartLocation(new Location(lobby, -60, 11, 71));
		ice.setFinishLocation(new Location(lobby, -50, 26, 114));
		ice.setCheckpointLocation(new Location(lobby, -56.5, 10, 71.5, 90, 0));
		ice.setSignLocation(new Location(lobby, -58, 10, 70));

		LobbyParkour ladder = new LobbyParkour();
		ladder.setName(ChatColor.LIGHT_PURPLE + "Странный");
		ladder.setStartLocation(new Location(lobby, -36, 11, 109));
		ladder.setFinishLocation(new Location(lobby, -50, 26, 114));
		ladder.setCheckpointLocation(new Location(lobby, -35.5, 10, 105.5, 0, 0));
		ladder.setSignLocation(new Location(lobby, -35, 10, 106));

		parkours.addAll(Lists.newArrayList(easy, medium, hard, insane, ice, ladder));
		Bukkit.getPluginManager().registerEvents(new LobbyParkourHandler(), Plugin.INSTANCE);
	}

	private static boolean isPassing(Player p, LobbyParkour parkour) {
		return isPassing(p) && passingParkours.get(p) == parkour;
	}

	public static boolean isPassing(Player p) {
		return passingParkours.containsKey(p);
	}

	private static void startPassing(Player p, LobbyParkour parkour) {
		passingParkours.put(p, parkour);
		passingTicks.put(p, 0);
		EntityUtils.sendActionBarInfo(p,
				ChatColor.DARK_GRAY + "[" + parkour.getFullName() + ChatColor.DARK_GRAY + "]" + ChatColor.BOLD + ChatColor.LIGHT_PURPLE + " Начато прохождение");
	}

	private static String getNormalTime(Player player) {
		return MathUtils.strictDecimal(getPassingTicks(player) / 20.0, 2);
	}

	private static int getPassingTicks(Player player) {
		return passingTicks.getOrDefault(player, 0);
	}

	private static int getRecordTicks(LobbyParkour parkour) {
		Sign sign = parkour.getSign();
		String strValue = sign.getLine(3);
		if(strValue.isEmpty()) return -1;
		try {
			return (int) Math.round(Double.parseDouble(ChatColor.stripColor(strValue)) * 20);
		} catch(Exception e) {
			return -1;
		}
	}

	private static boolean isRecord(LobbyParkour parkour, int ticks) {
		int recordTicks = getRecordTicks(parkour);
		return recordTicks == -1 || ticks < recordTicks;
	}

	private static void finishParkour(Player player, LobbyParkour parkour) {
		String record = isRecord(parkour, getPassingTicks(player)) ?ChatColor.GOLD + "" + ChatColor.BOLD + " Новый рекорд!" : "";
		EntityUtils.sendActionBarInfo(player,
				ChatColor.DARK_GRAY + "[" + parkour.getFullName() + ChatColor.DARK_GRAY + "]" + ChatColor.DARK_PURPLE + " Пройдено за " + ChatColor.DARK_AQUA
						+ getNormalTime(player) + ChatColor.DARK_PURPLE + "!" + record);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
		if(isRecord(parkour, getPassingTicks(player))) {
			Sign sign = parkour.getSign();
			sign.setLine(1, ChatColor.DARK_GREEN + "Рекорд:");
			sign.setLine(2, ChatColor.DARK_PURPLE + player.getName());
			sign.setLine(3, ChatColor.DARK_AQUA + getNormalTime(player));
			sign.update();
		}
		passingParkours.remove(player);
		passingTicks.remove(player);
	}

	public static void stopPassing(Player p) {
		passingParkours.remove(p);
		passingTicks.remove(p);
		toUpdate.remove(p);
	}

	public static void update() {
		for(Player player : WorldManager.getLobby().getPlayers()) {
			if(isPassing(player) && toUpdate.contains(player)) {
				passingTicks.replace(player, passingTicks.getOrDefault(player, 0) + 1);
				if(getPassingTicks(player) >= 20) {
					EntityUtils.sendActionBarInfo(player, ChatColor.GOLD + getNormalTime(player));
				}
			}
		}
	}

	@EventHandler
	public void checkMovement(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(PlayerHandler.isInLobby(player) && player.getActivePotionEffects().isEmpty()) {
			for(LobbyParkour parkour : parkours) {
				if(WorldUtils.compareIntegerLocations(parkour.getStartLocation(), player.getLocation())) {
					if(!isPassing(player, parkour)) {
						startPassing(player, parkour);
					}
					toUpdate.add(player);
				}
				if(isPassing(player, parkour) && WorldUtils.compareIntegerLocations(parkour.getFinishLocation(), player.getLocation())) {
					finishParkour(player, parkour);
				}
			}
			if(isPassing(player) && e.getTo().getY() <= 0) {
				EntityUtils.teleport(player, passingParkours.get(player).getCheckpointLocation(), false, true);
				stopPassing(player);
				passingTicks.replace(player, 0);
				toUpdate.remove(player);
			}
		}
	}

	@EventHandler
	public void cleanup(PlayerQuitEvent e) {
		if(PlayerHandler.isInLobby(e.getPlayer())) stopPassing(e.getPlayer());
	}

}
