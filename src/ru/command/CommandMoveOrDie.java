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
import ru.mutator.Mutator;
import ru.mutator.MutatorManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMoveOrDie implements CommandExecutor, TabCompleter {

	public static List<String> getMatchingStrings(String[] args, String... possibilities) {
		return getMatchingStrings(args, Arrays.asList(possibilities));
	}

	public static List<String> getMatchingStrings(String[] inputArgs, List<String> possibleCompletions) {
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

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.isOp()) return true;
		Player player = (Player) sender;
		if(args.length > 0) {
			if(args.length > 1 && args[0].equalsIgnoreCase("points")) {
				try {
					int score = Integer.parseInt(args[1]);
					MDPlayer md = MDPlayer.fromPlayer(player);
					if(md != null) md.setPoints(score);
				} catch(Exception e) {
					player.sendMessage(ChatColor.RED + "Invalid value");
				}
			}
			if(args.length > 1 && args[0].equalsIgnoreCase("score")) {
				try {
					int score = Integer.parseInt(args[1]);
					MDPlayer md = MDPlayer.fromPlayer(player);
					if(md != null) md.setScore(score);
				} catch(Exception e) {
					player.sendMessage(ChatColor.RED + "Invalid value");
				}
			}
			if(args[0].equalsIgnoreCase("damage")) {
				MoveOrDie.DO_MOVE_DAMAGE = !MoveOrDie.DO_MOVE_DAMAGE;
				player.sendMessage(ChatColor.GRAY + "Урон: " + MoveOrDie.DO_MOVE_DAMAGE);
			}
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
				if(args.length >= 2 && args[1].equalsIgnoreCase("ghost")) {
					PlayerHandler.giveGhostEffects(player);
				} else {
					PlayerHandler.givePlayerEffects(player);
				}
			}
			if(args.length > 1 && args[0].equalsIgnoreCase("mutator")) {
				if(args.length > 2 && args[1].equalsIgnoreCase("force")) {
					Mutator mutator = MutatorManager.getMutators().stream().filter(m -> m.getName().replaceAll(" ", "").equalsIgnoreCase(args[2])).findFirst()
							.orElse(null);
					if(mutator != null) {
						player.sendMessage(ChatColor.GREEN + "В след. раунде будет выбран мутатор: " + mutator.getName());
						MutatorManager.FORCED_MUTATOR = mutator;
					} else {
						player.sendMessage(ChatColor.RED + "Invalid mutator name");
					}
				}
				if(args[1].equalsIgnoreCase("disable")) {
					MutatorManager.FORCE_DISABLE = true;
				}
				if(args[1].equalsIgnoreCase("selector")) {
					MutatorManager.FORCE_SELECTOR = true;
				}
			}
			if(GameState.isPlaying() && args[0].equalsIgnoreCase("mode")) {
				if(args.length >= 3 && args[1].equalsIgnoreCase("switch")) {
					Mode toSwitch = ModeManager.getByID(args[2]);
					if(toSwitch != null) {
						ModeManager.endRound();
						if(ModeManager.getActiveMode() != null) {
							ModeManager.getActiveMode().onRoundEnd();
						}
						ModeManager.startNewRound(toSwitch);
					} else {
						player.sendMessage(ChatColor.RED + "Incorrect mode");
					}
				}
				if(args.length >= 2 && args[1].equalsIgnoreCase("sudden_death")) {
					ModeManager.initSuddenDeath();
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
			return getMatchingStrings(args, "start", "end", "mainworld", "parkour_reset", "effects", "mode", "damage", "score", "points", "mutator");
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("effects")) {
			return getMatchingStrings(args, "ghost");
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("mutator")) {
			return getMatchingStrings(args, "force", "selector", "disable");
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("mode")) {
			return getMatchingStrings(args, "switch", "sudden_death", "end");
		}
		if(args.length == 3 && args[0].equalsIgnoreCase("mutator") && args[1].equalsIgnoreCase("force")) {
			return getMatchingStrings(args,
					MutatorManager.getMutators().stream().map(mutator -> mutator.getName().replaceAll(" ", "")).collect(Collectors.toList()));
		}
		if(args.length == 3 && args[0].equalsIgnoreCase("mode") && args[1].equalsIgnoreCase("switch")) {
			return getMatchingStrings(args, ModeManager.getModes().stream().map(Mode::getID).collect(Collectors.toList()));
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("start")) {
			return getMatchingStrings(args, "mutator", "game");
		}
		return null;
	}

}
