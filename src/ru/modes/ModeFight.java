package ru.modes;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.InventoryUtils;
import ru.util.ItemUtils;
import ru.util.ParticleUtils;

public class ModeFight extends Mode {

	@Override
	public String getName() {
		return "Битва!";
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
	public void onRoundPrepare() {
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			Player player = mdPlayer.getPlayer();
			PlayerInventory inv = player.getInventory();
			inv.setItem(0, ItemUtils.setUnbreakable(new ItemStack(Material.IRON_SWORD)));
			Color color = ParticleUtils.toColor(mdPlayer.getColor());
			Material[] armor = new Material[] {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET};
			for(int slot = InventoryUtils.getBootsSlot(); slot <= InventoryUtils.getHelmetSlot(); slot++) {
				int index = slot - InventoryUtils.getBootsSlot();
				ItemStack item = ItemUtils.builder(armor[index]).unbreakable().build();
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(color);
				item.setItemMeta(meta);
				inv.setItem(slot, item);
			}
		}
	}

	@Override
	public void onPlayerDeath(MDPlayer mdPlayer) {
		Player player = mdPlayer.getPlayer();
		if(player.getKiller() != null) {
			Player killer = player.getKiller();
			MDPlayer killerMd = MDPlayer.fromPlayer(killer);
			if(killerMd != null) {
				killerMd.addPoint();
			}
		}
	}

	@Override
	public int getTime() {
		return 45;
	}

	@Override
	public boolean allowPVP() {
		return true;
	}

	@Override
	public boolean allowBlockBreaking() {
		return false;
	}

	@Override
	public boolean allowBlockPlacing() {
		return false;
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
	public boolean useSurvivalGameMode() {
		return false;
	}

}
