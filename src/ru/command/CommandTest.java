package ru.command;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CommandTest implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		Player p = (Player) sender;
		Location l;
		boolean looking = false;
		if(args.length > 0) {
			Block block = p.getTargetBlockExact(10);
			if(block != null) {
				l = block.getLocation();
				looking = true;
			} else {
				l = p.getLocation();
			}
		} else {
			l = p.getLocation();
		}
		String coords = l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();
		StringSelection sel = new StringSelection(coords);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		p.sendMessage((looking ? "Looking coordinates copied" : "Player coordinates copied") + ": " + coords);
		return true;
	}

}
