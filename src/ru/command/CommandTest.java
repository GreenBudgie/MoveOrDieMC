package ru.command;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.map.MapManager;
import ru.util.ParticleUtils;

public class CommandTest implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		Player p = (Player) sender;
		p.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "\u2718 \u2717 \u274C");
		return true;
	}

}
