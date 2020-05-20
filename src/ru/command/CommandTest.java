package ru.command;

import net.minecraft.server.v1_15_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_15_R1.WorldBorder;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.map.MapManager;
import ru.util.ParticleUtils;

public class CommandTest implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		Player p = (Player) sender;

		return true;
	}

}
