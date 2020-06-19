package ru.command;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.game.Rating;
import ru.game.WorldManager;
import ru.lobby.LobbyParkourHandler;

import java.util.List;

public class CommandRating implements CommandExecutor, TabCompleter {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if(args.length >= 2 && args[0].equalsIgnoreCase("player")) {
			Rating.printRating(player, args[1]);
			return true;
		}
		boolean round = true;
		if(args.length >= 1 && args[0].equalsIgnoreCase("game")) {
			round = false;
		}
		Rating.printLadder(player, round);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length == 1) {
			return CommandMoveOrDie.getMatchingStrings(args, "game", "round", "player");
		}
		if(args.length == 2) {
			return CommandMoveOrDie.getMatchingStrings(args, Lists.newArrayList(Rating.getRegisteredNames()));
		}
		return null;
	}

}
