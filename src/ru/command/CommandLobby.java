package ru.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.lobby.LobbyParkourHandler;

public class CommandLobby implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		if(!PlayerHandler.isPlaying(p)) {
			p.teleport(WorldManager.getLobby().getSpawnLocation());
			LobbyParkourHandler.stopPassing(p);
		}
		return true;
	}
}
