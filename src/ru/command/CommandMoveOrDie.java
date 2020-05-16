package ru.command;

import com.google.common.collect.Lists;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.game.MoveOrDie;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.lobby.LobbyParkour;
import ru.lobby.LobbyParkourHandler;

import java.util.Arrays;
import java.util.List;

public class CommandMoveOrDie implements CommandExecutor, TabCompleter {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		Player player = (Player) sender;
		if(args[0].equalsIgnoreCase("start")) {
			MoveOrDie.startGame();
		}
		if(args[0].equalsIgnoreCase("end")) {
			MoveOrDie.endGame();
		}
		if(args[0].equalsIgnoreCase("mainworld")) {
			player.setGameMode(GameMode.CREATIVE);
			player.setFlying(true);
			player.teleport(WorldManager.getOriginalGameWorld().getSpawnLocation());
		}
		if(args[0].equalsIgnoreCase("parkour_reset")) {
			for(LobbyParkour parkour : LobbyParkourHandler.parkours) {
				Sign sign = parkour.getSign();
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update();
			}
		}
		if(args[0].equalsIgnoreCase("effects")) {
			PlayerHandler.giveDefaultEffects(player);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length == 1) {
			return getMatchingStrings(args, "start", "end", "mainworld", "parkour_reset", "effects");
		}
		return null;
	}

	private static List<String> getMatchingStrings(String[] args, String... possibilities) {
		return getMatchingStrings(args, Arrays.asList(possibilities));
	}

	private static List<String> getMatchingStrings(String[] inputArgs, List<String> possibleCompletions) {
		String arg = inputArgs[inputArgs.length - 1];
		List<String> list = Lists.newArrayList();
		if(!possibleCompletions.isEmpty()) {
			for(String completion : possibleCompletions) {
				if(completion.startsWith(arg)) {
					list.add(completion);
				}
			}
		}
		return list;
	}

}
