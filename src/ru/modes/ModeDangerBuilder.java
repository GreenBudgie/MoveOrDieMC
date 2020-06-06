package ru.modes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.game.PlayerHandler;
import ru.util.Broadcaster;
import ru.util.ItemUtils;
import ru.util.MathUtils;

import java.util.*;

public class ModeDangerBuilder extends Mode implements Listener {

	private ListMultimap<Player, Location> placedBlocks = ArrayListMultimap.create();

	@Override
	public String getName() {
		return "Строитель";
	}

	@Override
	public String getDescription() {
		return "Убивай игроков, устанавливая перед ними смертельные блоки. Эти блоки можно ломать, добавляя их к себе в инвентарь.";
	}

	@Override
	public Material getItemToShow() {
		return Material.REDSTONE_BLOCK;
	}

	@Override
	public String getID() {
		return "DANGER_BUILDER";
	}

	@Override
	public void onRoundPrepare() {
		for(Player player : PlayerHandler.getPlayers()) {
			PlayerInventory inv = player.getInventory();
			inv.setItem(0, new ItemStack(Material.REDSTONE_BLOCK, 12));
			inv.setItem(1, ItemUtils.builder(Material.DIAMOND_PICKAXE).unbreakable().withEnchantments(new ItemUtils.Enchant(Enchantment.DIG_SPEED, 10)).build());
		}
	}

	@Override
	public void onRoundPreEnd() {
		placedBlocks.clear();
	}

	@Nullable
	public Player getWhoPlacedBlock(Block block) {
		for(Player player : placedBlocks.keySet()) {
			if(placedBlocks.get(player).contains(block.getLocation())) return player;
		}
		return null;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockPlace(BlockPlaceEvent e) {
		if(!e.isCancelled()) {
			if(PlayerHandler.isPlaying(e.getPlayer())) {
				placedBlocks.put(e.getPlayer(), e.getBlock().getLocation());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockBreak(BlockBreakEvent e) {
		if(!e.isCancelled()) {
			boolean toCancel = true;
			for(Player currentPlayer : placedBlocks.keySet()) {
				List<Location> blocksPlacedByPlayer = placedBlocks.get(currentPlayer);
				if(!blocksPlacedByPlayer.isEmpty()) {
					if(blocksPlacedByPlayer.contains(e.getBlock().getLocation())) {
						toCancel = false;
						Player player = e.getPlayer();
						ItemStack block = player.getInventory().getItem(0);
						if(block == null) {
							player.getInventory().setItem(0, new ItemStack(Material.REDSTONE_BLOCK));
						} else {
							block.setAmount(Math.min(block.getAmount() + 1, 64));
						}
						blocksPlacedByPlayer.remove(e.getBlock().getLocation());
					}
				}
			}
			if(toCancel) e.setCancelled(true);
		}
	}


	@Override
	public int getTime() {
		return 50;
	}

	@Override
	public boolean allowBlockPlacing() {
		return true;
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
	public boolean allowBlockBreaking() {
		return true;
	}

	@Override
	public boolean useSurvivalGameMode() {
		return true;
	}

	@Override
	public boolean allowPVP() {
		return false;
	}

}
