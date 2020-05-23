package ru.modes;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.game.PlayerHandler;
import ru.util.ItemUtils;

public class ModeSpleef extends Mode {

	private final Material FLOOR_TYPE = Material.CYAN_TERRACOTTA;

	@Override
	public String getName() {
		return "�����";
	}

	@Override
	public String getDescription() {
		return "�������� ������� ����, ����� ��� ���� �����";
	}

	@Override
	public Material getItemToShow() {
		return Material.DIAMOND_PICKAXE;
	}

	@Override
	public String getID() {
		return "SPLEEF";
	}

	@Override
	public void onRoundPrepare() {
		for(Player player : PlayerHandler.getPlayers()) {
			PlayerInventory inv = player.getInventory();
			ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
			net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(pickaxe);
			NBTTagCompound nbt = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
			NBTTagList canDestroy = new NBTTagList();
			canDestroy.add(NBTTagString.a("minecraft:" + FLOOR_TYPE.name().toLowerCase()));
			nbt.set("CanDestroy", canDestroy);
			nmsItem.setTag(nbt);
			pickaxe = CraftItemStack.asBukkitCopy(nmsItem);
			ItemUtils.addEnchantments(pickaxe, new ItemUtils.Enchant(Enchantment.DIG_SPEED, 10));
			ItemUtils.setUnbreakable(pickaxe);
			inv.setItem(0, pickaxe);
		}
	}

	@Override
	public boolean usePoints() {
		return false;
	}

	@Override
	public int getTime() {
		return 30;
	}

	@Override
	public boolean allowBlockBreaking() {
		return true;
	}

}
