package ru.modes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.InventoryUtils;
import ru.util.ItemUtils;

public class ModeFight extends Mode {

	@Override
	public String getName() {
		return ChatColor.DARK_RED + "" + ChatColor.BOLD + "Битва!";
	}

	@Override
	public String getDescription() {
		return "Дерись... А чего еще можно было ожидать?";
	}

	@Override
	public Material getItemToShow() {
		return Material.IRON_SWORD;
	}

	@Override
	public String getID() {
		return "FIGHT";
	}

	@Override
	public void onStart() {
		for(Player player : PlayerHandler.getPlayers()) {
			PlayerInventory inv = player.getInventory();
			inv.setItem(0, ItemUtils.setUnbreakable(new ItemStack(Material.IRON_SWORD)));
			inv.setBoots(ItemUtils.setUnbreakable(new ItemStack(Material.CHAINMAIL_BOOTS)));
			inv.setLeggings(ItemUtils.setUnbreakable(new ItemStack(Material.CHAINMAIL_LEGGINGS)));
			inv.setChestplate(ItemUtils.setUnbreakable(new ItemStack(Material.CHAINMAIL_CHESTPLATE)));
			inv.setHelmet(ItemUtils.setUnbreakable(new ItemStack(Material.CHAINMAIL_HELMET)));
		}
	}

	@Override
	public boolean allowPVP() {
		return true;
	}

}
