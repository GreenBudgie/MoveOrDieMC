package ru.modes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import java.util.HashSet;
import java.util.Set;

public class ModeDangerBuilder extends Mode implements Listener {

	private Set<Location> placedBlocks = new HashSet<>();

	@Override
	public String getName() {
		return "Опасный Строитель";
	}

	@Override
	public String getDescription() {
		return "Убивай игроков, ставя перед ними смертельные блоки";
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
			inv.setItem(1, ItemUtils.builder(Material.DIAMOND_PICKAXE).unbreakable().withEnchantments(new ItemUtils.Enchant(Enchantment.DIG_SPEED, 5)).build());
		}
	}

	@Override
	public int getTime() {
		return 60;
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
			if(PlayerHandler.isPlaying(e.getPlayer())) {
				placedBlocks.add(e.getBlock().getLocation());
				e.getItemInHand().setAmount(64);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void blockBreak(BlockBreakEvent e) {
		if(!e.isCancelled()) {
			if(!placedBlocks.contains(e.getBlock().getLocation())) {
				e.setCancelled(true);
				return;
			}
			if(PlayerHandler.isPlaying(e.getPlayer())) {
				placedBlocks.remove(e.getBlock().getLocation());
			}
		}
	}

}
