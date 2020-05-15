package ru.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import ru.lobby.LobbyEntertainmentHandler;
import ru.lobby.LobbyParkourHandler;
import ru.lobby.sign.LobbySignManager;
import ru.start.Plugin;
import ru.util.TaskManager;

public class MoveOrDie implements Listener {

	public static void init() {
		Bukkit.getPluginManager().registerEvents(new MoveOrDie(), Plugin.INSTANCE);
		Bukkit.getPluginManager().registerEvents(new PlayerHandler(), Plugin.INSTANCE);
		WorldManager.init();
		TaskManager.init();
		LobbyParkourHandler.init();
		LobbyEntertainmentHandler.init();
		LobbySignManager.init();
	}

	public static void startGame() {

	}

	public static void endGame() {

	}

	public static void update() {
		LobbyParkourHandler.update();
	}

}
