package ru.game;

import org.bukkit.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldManager {

	private static World lobby, originalGameWorld, currentGameWorld;

	public static void init() {
		lobby = Bukkit.getWorld("Lobby");
		lobby.setDifficulty(Difficulty.PEACEFUL);
		lobby.setPVP(false);
		lobby.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		lobby.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		lobby.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		lobby.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		lobby.setGameRule(GameRule.DO_INSOMNIA, false);

		originalGameWorld = Bukkit.createWorld(new WorldCreator("GameWorld"));
		originalGameWorld.setDifficulty(Difficulty.PEACEFUL);
		originalGameWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		originalGameWorld.setGameRule(GameRule.NATURAL_REGENERATION, false);
		originalGameWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
		originalGameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		originalGameWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		originalGameWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		originalGameWorld.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
		originalGameWorld.setGameRule(GameRule.FALL_DAMAGE, false);
		originalGameWorld.setGameRule(GameRule.DO_INSOMNIA, false);
		originalGameWorld.setGameRule(GameRule.DO_TILE_DROPS, false);
		originalGameWorld.setGameRule(GameRule.MOB_GRIEFING, false);
		originalGameWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
		originalGameWorld.setPVP(false);
		originalGameWorld.setTime(6000);
		
		if(Bukkit.getWorld("GameWorldTemp") != null) {
			currentGameWorld = Bukkit.getWorld("GameWorldTemp");
			currentGameWorld.setDifficulty(Difficulty.PEACEFUL);
		}
	}

	public static void makeWorld() {
		if(!hasWorld()) {
			currentGameWorld = copyAsTemp(originalGameWorld);
		}
	}

	public static void deleteWorld() {
		if(hasWorld()) {
			deleteTempWorld(currentGameWorld);
			currentGameWorld = null;
		}
	}

	private static boolean deleteWorld(World world) {
		Bukkit.unloadWorld(world, false);
		return deleteWorld(world.getWorldFolder());
	}

	private static boolean deleteWorld(File path) {
		if(path.exists()) {
			File[] files = path.listFiles();
			if(files == null) return false;
			for(File file : files) {
				if(file.isDirectory()) {
					deleteWorld(file);
				} else {
					file.delete();
				}
			}
		}
		return path.delete();
	}

	public static World copyAsTemp(World world) {
		File source = world.getWorldFolder();
		File target = new File(source.getAbsolutePath() + "Temp");
		copyWorld(source, target);
		try {
			new File(target.getAbsolutePath() + "/temp.info").createNewFile();
		} catch(IOException e) {
		}
		return Bukkit.createWorld(new WorldCreator(world.getName() + "Temp"));
	}

	private static void copyWorld(File source, File target) {
		try {
			ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.dat"));
			if(!ignore.contains(source.getName())) {
				if(source.isDirectory()) {
					if(!target.exists()) target.mkdirs();
					String files[] = source.list();
					for(String file : files) {
						File srcFile = new File(source, file);
						File destFile = new File(target, file);
						copyWorld(srcFile, destFile);
					}
				} else {
					InputStream in = new FileInputStream(source);
					OutputStream out = new FileOutputStream(target);
					byte[] buffer = new byte[1024];
					int length;
					while((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
					in.close();
					out.close();
				}
			}
		} catch(IOException e) {
		}
	}

	public static boolean deleteTempWorld(World world) {
		if(!(new File(world.getWorldFolder().getAbsolutePath() + "/temp.info").exists())) {
			return false;
		}

		return deleteWorld(world);
	}

	public static World getLobby() {
		return lobby;
	}

	public static World getOriginalGameWorld() {
		return originalGameWorld;
	}

	public static World getCurrentGameWorld() {
		return currentGameWorld;
	}

	public static boolean hasWorld() {
		return currentGameWorld != null;
	}

}
