package ru.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.game.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandOpStat implements CommandExecutor, TabCompleter {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("hardreset")) {
				for(String name : Rating.getRegisteredNames()) {
					Rating.removeRating(name);
					sender.sendMessage(ChatColor.DARK_GREEN + "Removed rating for: " + ChatColor.GOLD + name);
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("cleanup")) {
				for(String name : Rating.getRegisteredNames()) {
					boolean toRemove = true;
					for(Rating stat : Rating.values()) {
						if(stat.getValue(name) != 0) {
							toRemove = false;
							break;
						}
					}
					if(toRemove) {
						Rating.removeRating(name);
						sender.sendMessage(ChatColor.DARK_GREEN + "Removed zero-valued rating for: " + ChatColor.GOLD + name);
					}
				}
				return true;
			}
		}
		if(args.length >= 2) {
			String enteredName = args[1];
			Set<String> names = enteredName.equalsIgnoreCase("all") ? Rating.getRegisteredNames() : Sets.newHashSet(enteredName);
			for(String name : names) {
				if(args[0].equalsIgnoreCase("remove")) {
					if(!Rating.isRegistered(name)) {
						sender.sendMessage(ChatColor.GOLD + name + ChatColor.DARK_RED + "" + ChatColor.BOLD + " is not registered");
						continue;
					}
					Rating.removeRating(name);
					sender.sendMessage(ChatColor.DARK_GREEN + "Removed " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " from rating");
				}
				if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) {
					if(args.length < 4) {
						sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Stat or value is not specified!");
						return true;
					}
					String enteredStat = args[2];
					Set<Rating> stats;
					if(enteredStat.equalsIgnoreCase("all")) {
						stats = Sets.newHashSet(Rating.values());
					} else {
						try {
							stats = Sets.newHashSet(Rating.valueOf(enteredStat));
						} catch(IllegalArgumentException e) {
							sender.sendMessage(ChatColor.DARK_GREEN + enteredStat + ChatColor.DARK_RED + "" + ChatColor.BOLD + " does not exists!");
							return true;
						}
					}
					int value;
					try {
						value = Integer.parseInt(args[3]);
					} catch(NumberFormatException e) {
						sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Illegal value: " + ChatColor.RESET + ChatColor.AQUA + args[3]);
						return true;
					}
					for(Rating stat : stats) {
						if(args[0].equalsIgnoreCase("set")) {
							stat.setValue(name, value);
							sender.sendMessage(ChatColor.GREEN + stat.name() + ChatColor.DARK_GREEN + " is now " + ChatColor.AQUA + stat.getValue(name)
									+ ChatColor.DARK_GREEN + " for " + ChatColor.GOLD + name);
						}
						if(args[0].equalsIgnoreCase("add")) {
							stat.addValue(name, value);
							sender.sendMessage(ChatColor.GREEN + stat.name() + ChatColor.DARK_GREEN + " is now " + ChatColor.AQUA + stat.getValue(name)
									+ ChatColor.DARK_GREEN + " for " + ChatColor.GOLD + name);
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length == 1) {
			return CommandMoveOrDie.getMatchingStrings(args, "set", "add", "remove", "hardreset", "cleanup");
		}
		if(args.length == 2 && Lists.newArrayList("set", "add", "remove").contains(args[0].toLowerCase())) {
			List<String> matching = new ArrayList<>();
			matching.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
			matching.addAll(Rating.getRegisteredNames());
			matching.add("all");
			matching = matching.stream().distinct().collect(Collectors.toList());
			return CommandMoveOrDie.getMatchingStrings(args, matching);
		}
		if(args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
			List<String> matching = Stream.of(Rating.values()).map(Rating::toString).collect(Collectors.toList());
			matching.add("all");
			return CommandMoveOrDie.getMatchingStrings(args, matching);
		}
		return null;
	}

}
