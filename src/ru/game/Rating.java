package ru.game;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.start.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public enum Rating {

	GAMES_PLAYED("Игр сыграно"), GAMES_WON("Игр выиграно"), ROUNDS_PLAYED("Раундов сыграно"), ROUNDS_WON("Раундов выиграно");

	public static boolean ratingEnabled = true;
	private static File ratingFile = new File(Plugin.INSTANCE.getDataFolder() + File.separator + "rating.yml");
	private static YamlConfiguration ratingConfig = YamlConfiguration.loadConfiguration(ratingFile);

	String desc;

	Rating(String desc) {
		this.desc = desc;
	}

	public static void save() {
		try {
			ratingConfig.save(ratingFile);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private String getPath(String name) {
		return name + "." + this.name();
	}

	public int getValue(String name) {
		return ratingConfig.getInt(getPath(name), 0);
	}

	public void setValue(String name, int value) {
		if(ratingEnabled) {
			ratingConfig.set(getPath(name), value);
		}
	}

	public void addValue(String name, int value) {
		if(ratingEnabled) {
			ratingConfig.set(getPath(name), getValue(name) + value);
		}
	}

	public void increaseValue(String name) {
		addValue(name, 1);
	}

	public static void removeRating(String name) {
		ratingConfig.set(name, null);
	}

	public static Set<String> getRegisteredNames() {
		return ratingConfig.getValues(false).keySet();
	}

	public static boolean isRegistered(String name) {
		return getRegisteredNames().contains(name);
	}

	public static List<String> getRoundWinRateLadder() {
		List<String> names = Lists.newArrayList(getRegisteredNames());
		names.sort(Comparator.comparingDouble(Rating::getRoundWinRate));
		return names;
	}

	public static void printLadder(Player player) {
		if(getRegisteredNames().isEmpty()) {
			player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Нет информации");
			return;
		}
		player.sendMessage(ChatColor.DARK_AQUA + "Рейтинг игроков по" + ChatColor.BOLD + " винрейту раундов" + ChatColor.RESET + ChatColor.GRAY + ":");
		List<String> ladder = getRoundWinRateLadder();
		for(int i = 0; i < ladder.size(); i++) {
			String name = ladder.get(i);
			player.sendMessage(ChatColor.DARK_GREEN + "#" + ChatColor.GREEN + (i + 1) + ChatColor.GOLD + " " + name + ChatColor.GRAY + ", " +
					ChatColor.AQUA + ChatColor.BOLD + (int) (getRoundWinRate(name) * 100) + ChatColor.DARK_AQUA + "%");
		}
	}

	public static void printRating(Player player, String name) {
		if(!isRegistered(name)) {
			player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Нет информации");
			return;
		}
		player.sendMessage(ChatColor.DARK_AQUA + "Рейтинг " + ChatColor.GOLD + name + ChatColor.GRAY + ":");
		player.sendMessage(ChatColor.GREEN + "Винрейт по раундам" + ChatColor.GRAY + ": " + ChatColor.AQUA +
				ChatColor.BOLD + (int) (getRoundWinRate(name) * 100) + ChatColor.DARK_AQUA + "%");
		player.sendMessage(ChatColor.DARK_GREEN + "Винрейт по играм" + ChatColor.GRAY + ": " + ChatColor.AQUA +
				ChatColor.BOLD + (int) (getGameWinRate(name) * 100) + ChatColor.DARK_AQUA + "%");
		for(Rating stat : Rating.values()) {
			player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.YELLOW + stat.getDescription() + ChatColor.GRAY + ": " + ChatColor.DARK_AQUA + ChatColor.BOLD + stat.getValue(name));
		}
	}

	public static double getRoundWinRate(String name) {
		int played = ROUNDS_PLAYED.getValue(name);
		int won = ROUNDS_WON.getValue(name);
		if(won == 0 || played == 0) return 0;
		return (double) won / played;
	}

	public static double getGameWinRate(String name) {
		int played = GAMES_PLAYED.getValue(name);
		int won = GAMES_WON.getValue(name);
		if(won == 0 || played == 0) return 0;
		return (double) won / played;
	}

	public String getDescription() {
		return desc;
	}




}
