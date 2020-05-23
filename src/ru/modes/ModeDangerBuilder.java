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

import java.util.*;

public class ModeDangerBuilder extends Mode implements Listener {

	private ListMultimap<Player, Location> placedBlocks = ArrayListMultimap.create();

	@Override
	public String getName() {
		return "Опасный Строитель";
	}

	@Override
	public String getDescription() {
		return "Убивай игроков, ставя перед ними смертельные блоки. Блоки нельзя ставить друг на друга!";
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
			inv.setItem(0, new ItemStack(Material.REDSTONE_BLOCK, 64));
			inv.setItem(1, ItemUtils.builder(Material.DIAMOND_PICKAXE).unbreakable().withEnchantments(new ItemUtils.Enchant(Enchantment.DIG_SPEED, 10)).build());
		}
	}

	@Nullable
	public Player getWhoPlacedBlock(Block block) {
		for(Player player : placedBlocks.keySet()) {
			if(placedBlocks.get(player).contains(block.getLocation())) return player;
		}
		return null;
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
	public boolean allowBlockBreaking() {
		return true;
	}

	public boolean useSurvivalGameMode() {
		return true;
	}

	@Override
	public void onRoundPreEnd() {
		placedBlocks.clear();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockPlace(BlockPlaceEvent e) {
		if(!e.isCancelled()) {
			Block against = e.getBlockAgainst();
			if(against.getType() == Material.REDSTONE_BLOCK) {
				e.setCancelled(true);
			} else {
				if(PlayerHandler.isPlaying(e.getPlayer())) {
					placedBlocks.put(e.getPlayer(), e.getBlock().getLocation());
					e.getItemInHand().setAmount(64);
				}
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
						blocksPlacedByPlayer.remove(e.getBlock().getLocation());
					}
				}
			}
			if(toCancel) e.setCancelled(true);
		}
	}

}
