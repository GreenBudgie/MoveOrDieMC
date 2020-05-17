package ru.mutator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.map.MapMutator;
import ru.modes.ModeManager;
import ru.start.Plugin;
import ru.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutatorSelector implements Listener {

	private static final int STATE_TIME = 15;
	private static Mutator selectedMutator = null;
	private static MDPlayer selectorPlayer = null;
	private static MutatorSelector instance;
	private static List<Mutator> mutatorsToSelect = new ArrayList<>();
	private static boolean selecting = false;

	public static void start() {
		List<Location> locations = Lists.newArrayList(MapMutator.getSpawns());
		Collections.shuffle(locations);
		for(int i = 0; i < PlayerHandler.getPlayers().size(); i++) {
			Player player = PlayerHandler.getPlayers().get(i);
			PlayerHandler.resetNoEffects(player);
			player.setGameMode(GameMode.ADVENTURE);
			EntityUtils.teleportCentered(player, locations.get(i), true, true);
		}
		GameState.setTimer(STATE_TIME);
		GameState.MUTATOR.set();
		Bukkit.getPluginManager().registerEvents(instance = new MutatorSelector(), Plugin.INSTANCE);
	}

	public static void update() {
		if(TaskManager.isSecUpdated()) {
			if(GameState.updateTimer()) {
				finish();
			} else {
				if(GameState.getTimer() == 13) {
					Broadcaster.each(PlayerHandler.getPlayers()).title("", ChatColor.GREEN + "Мутатор выбирает", 20, 60, 10);
				}
				if(GameState.getTimer() == 11) {
					List<MDPlayer> players = Lists.newArrayList(PlayerHandler.getMDPlayers());
					if(selectorPlayer != null) players.remove(selectorPlayer);
					selectorPlayer = MathUtils.choose(players);
					EntityUtils.teleport(selectorPlayer.getPlayer(), MapMutator.getMutatorSelectorSpawn());
					List<Mutator> mutators = Lists.newArrayList(MutatorManager.getMutators());
					if(selectedMutator != null) mutators.remove(selectedMutator);
					Broadcaster br = Broadcaster.each(PlayerHandler.getPlayers());
					br.title(selectorPlayer.getColor() + selectorPlayer.getNickname(), null, 0, 60, 10);
					br.toChat(ChatColor.GRAY + StringUtils.repeat("-", 20), "");
					for(int i = 0; i < 3; i++) {
						Mutator current = MathUtils.choose(mutators);
						mutatorsToSelect.add(current);
						mutators.remove(current);
						br.toChat(ChatColor.YELLOW + String.valueOf(i + 1) + ChatColor.GRAY + ". " + current.getName());
						selectorPlayer.getPlayer().getInventory().setItem(2 + i * 2, ItemUtils.builder(current.getItemToShow()).withName(current.getName()).build());
					}
					br.toChat("", ChatColor.GRAY + StringUtils.repeat("-", 20));
					selecting = true;
				}
				if(GameState.getTimer() <= 10 && GameState.getTimer() > 3) {
					EntityUtils.sendActionBarInfo(selectorPlayer.getPlayer(), ChatColor.DARK_RED + "" + ChatColor.BOLD + (GameState.getTimer() - 3));
				}
				if(GameState.getTimer() == 3 && !selecting) {
					onSelect();
				}
			}
		}
	}

	private static void onSelect() {
		if(selectedMutator == null) {
			selectedMutator = MathUtils.choose(mutatorsToSelect);
		}
		Broadcaster br = Broadcaster.each(PlayerHandler.getPlayers());
		br.title(selectedMutator.getName(), ChatColor.GREEN + "Новый мутатор", 5, 60, 10);
		br.toChat(ChatColor.GRAY + "----- " + selectedMutator.getName() + ChatColor.GRAY + " -----", "");
		br.toChat(ChatColor.GREEN + selectedMutator.getDescription());
		br.toChat("", ChatColor.GRAY + StringUtils.repeat("-", 12 + ChatColor.stripColor(selectedMutator.getName()).length()));
		selectorPlayer.getPlayer().getInventory().clear();
		mutatorsToSelect.clear();
		selecting = false;
	}

	public static void finish() {
		unregister();
		ModeManager.startNewRound();
	}

	public static Mutator getSelectedMutator() {
		return selectedMutator;
	}

	public static void cleanup() {
		selectedMutator = null;
		selectorPlayer = null;
		mutatorsToSelect.clear();
		selecting = false;
		unregister();
	}

	public static void unregister() {
		if(instance != null) {
			HandlerList.unregisterAll(instance);
			instance = null;
		}
	}

	@EventHandler
	public void selectMutator(PlayerInteractEvent e) {
		if(selecting && e.getPlayer() == selectorPlayer.getPlayer()) {
			ItemStack item = e.getItem();
			if(item != null) {
				for(Mutator mutator : mutatorsToSelect) {
					if(mutator.getItemToShow() == item.getType()) {
						selectedMutator = mutator;
						GameState.setTimer(3);
						onSelect();
						break;
					}
				}
			}
		}
	}

}
