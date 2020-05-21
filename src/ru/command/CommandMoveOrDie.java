package ru.command;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.game.*;
import ru.lobby.LobbyParkour;
import ru.lobby.LobbyParkourHandler;
import ru.modes.Mode;
import ru.modes.ModeManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMoveOrDie implements CommandExecutor, TabCompleter {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		Player player = (Player) sender;
		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("start")) {
				if(args.length > 1) {
					if(args[1].equalsIgnoreCase("mutator")) GameSetupManager.FAST_START = 1;
					if(args[1].equalsIgnoreCase("game")) GameSetupManager.FAST_START = 2;
				}
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
				PlayerHandler.givePlayerEffects(player);
			}
			if(GameState.isPlaying() && args[0].equalsIgnoreCase("mode")) {
				if(args.length >= 3 && args[1].equalsIgnoreCase("switch")) {
					Mode toSwitch = ModeManager.getByID(args[2]);
					if(toSwitch != null) {
						ModeManager.endRound();
						ModeManager.startNewRound(toSwitch);
					} else {
						player.sendMessage(ChatColor.RED + "Incorrect mode");
					}
				}
				if(args.length >= 2 && args[1].equalsIgnoreCase("end")) {
					ModeManager.endRound();
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length == 1) {
			return getMatchingStrings(args, "start", "end", "mainworld", "parkour_reset", "effects", "mode");
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("mode")) {
			return getMatchingStrings(args, "switch", "end");
		}
		if(args.length == 3 && args[0].equalsIgnoreCase("mode") && args[1].equalsIgnoreCase("switch")) {
			return getMatchingStrings(args, ModeManager.getModes().stream().map(Mode::getID).collect(Collectors.toList()));
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("start")) {
			return getMatchingStrings(args, "mutator", "game");
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
