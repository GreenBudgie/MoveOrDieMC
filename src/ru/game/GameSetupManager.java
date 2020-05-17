package ru.game;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.modes.Mode;
import ru.modes.ModeManager;
import ru.mutator.MutatorSelector;
import ru.util.TaskManager;

public class GameSetupManager {

	public static void update() {
		if(TaskManager.isSecUpdated()) {
			if(GameState.updateTimer()) {
				MutatorSelector.start();
			} else {
				if(GameState.getTimer() == 15) {
					for(Player player : PlayerHandler.getPlayers()) {
						player.sendTitle("", ChatColor.GREEN + "Добро пожаловать в", 20, 100, 20);
					}
				}
				if(GameState.getTimer() == 13) {
					for(Player player : PlayerHandler.getPlayers()) {
						player.sendTitle(MoveOrDie.getLogo(), null, 0, 60, 20);
					}
				}
				if(GameState.getTimer() == 8) {
					Mode mode = ModeManager.selectRandomMode();
					for(Player player : PlayerHandler.getPlayers()) {
						player.sendTitle(mode.getName(), ChatColor.GREEN + "Следующий режим", 10, 60, 20);
						player.sendMessage(ChatColor.GRAY + "----- " + mode.getName() + ChatColor.RESET + ChatColor.GRAY + " -----");
						player.sendMessage("");
						player.sendMessage(ChatColor.GREEN + mode.getDescription());
						player.sendMessage("");
						player.sendMessage(ChatColor.GRAY + StringUtils.repeat("-", 12 + ChatColor.stripColor(mode.getName()).length()));
					}
				}
				if(GameState.getTimer() < 3) {
					for(Player player : PlayerHandler.getPlayers()) {
						player.sendTitle(ChatColor.DARK_GREEN + String.valueOf(GameState.getTimer() + 1), "", 0, 30, 0);
					}
				}
			}
		}
	}

}
