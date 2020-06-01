package ru.game;

import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.modes.ModeManager;
import ru.util.Broadcaster;
import ru.util.EntityUtils;
import ru.util.WorldUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerHandler implements Listener {

	private static List<MDPlayer> players = new ArrayList<>();
	//Handles player's deaths queue. It uses a list inside so that to handle simultaneous deaths
	private static List<Set<MDPlayer>> deathQueue = new ArrayList<>();
	//Draw handling
	private static final int maxDeathHandleDelay = 5;
	private static int deathHandleDelay = -1;
	private static Set<MDPlayer> lastDeaths = new HashSet<>();

	private static Map<String, HPDisplay> hpDisplay = new HashMap<>();

	public enum HPDisplay {
		ACTIONBAR("Над инвентарем"), BOSSBAR("Сверху экрана"), BOTH("Сверху и над инвентарем");

		public String name;

		HPDisplay(String name) {
			this.name = name;
		}
	}

	public static HPDisplay getHPDisplay(Player player) {
		return hpDisplay.getOrDefault(player.getName(), HPDisplay.ACTIONBAR);
	}

	public static void setHpDisplay(Player player, HPDisplay display) {
		hpDisplay.getOrDefault(player.getName(), display);
	}

	public static void cycleHPDisplay(Player player) {
		HPDisplay prev = getHPDisplay(player);
		for(int i = 0; i < HPDisplay.values().length; i++) {
			HPDisplay current = HPDisplay.values()[i];
			if(prev == current) {
				hpDisplay.put(player.getName(), HPDisplay.values()[i == HPDisplay.values().length - 1 ? 0 : i + 1]);
				break;
			}
		}
	}

	public static List<Player> getPlayers() {
		return players.stream().map(MDPlayer::getPlayer).collect(Collectors.toList());
	}

	public static List<MDPlayer> getMDPlayers() {
		return players;
	}

	public static boolean isInLobby(Player p) {
		return WorldManager.getLobby().getPlayers().contains(p)|| WorldManager.getOriginalGameWorld().getPlayers().contains(p);
	}

	public static boolean isPlaying(Player player) {
		return MDPlayer.fromPlayer(player) != null;
	}

	public static void update() {
		for(MDPlayer mdPlayer : getMDPlayers()) {
			mdPlayer.update();
		}
		if(deathHandleDelay != -1) {
			if(deathHandleDelay == 0) {
				handleDeathResult();
				deathHandleDelay = -1;
			} else {
				deathHandleDelay--;
			}
		}
	}

	public static void reset(Player player) {
		player.getActivePotionEffects().forEach(ef -> player.removePotionEffect(ef.getType()));
		resetNoEffects(player);
	}

	public static List<MDPlayer> getGhosts() {
		return players.stream().filter(MDPlayer::isGhost).collect(Collectors.toList());
	}

	public static List<MDPlayer> getAlive() {
		return players.stream().filter(player -> !player.isGhost()).collect(Collectors.toList());
	}

	public static List<Set<MDPlayer>> getDeathQueue() {
		return deathQueue;
	}

	public static void resurrectAll() {
		for(MDPlayer mdPlayer : getGhosts()) {
			mdPlayer.resurrect();
		}
	}

	public static void clearDeathQueue() {
		deathQueue.clear();
	}

	public static void handleDeathResult() {
		if(GameState.GAME.isRunning()) {
			lastDeaths.removeIf(MDPlayer::isLeft);
			if(!lastDeaths.isEmpty()) {
				deathQueue.add(Sets.newHashSet(lastDeaths));
			}
			if(getAlive().size() <= 1) {
				ModeManager.endRound();
			}
			lastDeaths.clear();
			deathHandleDelay = -1;
		}
	}

	public static void setDeathHandle(MDPlayer mdPlayer) {
		if(GameState.GAME.isRunning()) {
			if(!mdPlayer.isLeft()) lastDeaths.add(mdPlayer);
			if(deathHandleDelay == -1) {
				deathHandleDelay = maxDeathHandleDelay;
			}
		}
	}

	public static void resetNoEffects(Player player) {
		player.getInventory().clear();
		EntityUtils.heal(player);
		player.setFireTicks(0);
		player.setNoDamageTicks(0);
		player.setExp(0);
		player.setLevel(0);
		player.setWalkSpeed(0.2F);
	}

	public static void givePlayerEffects(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
	}

	public static void giveGhostEffects(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		e.setCancelled(true);
		Player sender = e.getPlayer();
		String msg = e.getMessage();
		if(isInLobby(sender)) {
			Broadcaster.inWorld(WorldManager.getLobby()).toChat(ChatColor.GOLD + sender.getName() + ChatColor.GRAY + ChatColor.BOLD + " > " + ChatColor.RESET + ChatColor.WHITE + msg);
		}
		if(isPlaying(sender)) {
			MDPlayer mdPlayer = MDPlayer.fromPlayer(sender);
			if(mdPlayer != null) {
				Broadcaster.each(getPlayers()).toChat(mdPlayer.getColor() + mdPlayer.getNickname() + ChatColor.GRAY + ChatColor.BOLD + " > " + ChatColor.RESET + ChatColor.WHITE + msg);
			}
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent e) {
		e.setDeathMessage(null);
		Player player = e.getEntity();
		if(isPlaying(player)) {
			e.setDroppedExp(0);
			e.getDrops().clear();
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null) mdPlayer.onDeath();
		}
	}

	@EventHandler
	public void handSwap(PlayerSwapHandItemsEvent e) {
		if(isPlaying(e.getPlayer())) {
			MDPlayer player = MDPlayer.fromPlayer(e.getPlayer());
			if(player != null) {
				player.switchSidebarSize();
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void move(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(isPlaying(player)) {
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(GameState.GAME.isRunning()) {
				if(mdPlayer != null && player.isOnGround() && !WorldUtils.compareLocations(e.getFrom(), e.getTo())) {
					if(player.isSprinting()) {
						mdPlayer.handleSprintHp();
					} else {
						if(!player.isSneaking()) mdPlayer.handleWalkHp();
					}
				}
			}
			if(GameState.ROUND_START.isRunning()) {
				if(!WorldUtils.compareLocations(e.getFrom(), e.getTo())) e.setCancelled(true);
			}
		}
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
