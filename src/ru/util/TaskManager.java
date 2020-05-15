package ru.util;

import org.bukkit.Bukkit;
import ru.game.MoveOrDie;
import ru.start.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskManager {

	private static int tick = 0;
	private static long fullTicks = 0;
	private static int sec = 0;
	private static long fullSeconds = 0;
	private static int min = 0;
	private static long fullMinutes = 0;

	public static void init() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.INSTANCE, () -> {

			MoveOrDie.update();
			if(tick < 19) {
				tick++;
				fullTicks++;
			} else {
				tick = 0;
				if(sec < 59) {
					sec++;
					fullSeconds++;
				} else {
					sec = 0;
					if(min < 59) {
						min++;
						fullMinutes++;
					} else {
						min = 0;
					}
				}
			}

		}, 0L, 1L);

	}

	public static boolean ticksPassed(int ticks) {
		return fullTicks % ticks == 0;
	}

	public static boolean secondsPassed(int seconds) {
		return isSecUpdated() && fullSeconds % seconds == 0;
	}

	public static boolean minutesPassed(int minutes) {
		return isMinUpdated() && fullMinutes % minutes == 0;
	}

	/**
	 * Converts ticks to seconds
	 * @param ticks Ticks to convert
	 * @return Number of seconds in the given number of ticks
	 */
	public static int ticksToSeconds(int ticks) {
		return (int) Math.round(ticks / 20.0);
	}

	/**
	 * Formats the given time in seconds to minutes:seconds form
	 * @param seconds Seconds to format
	 * @return Result in minutes:seconds form
	 */
	public static String formatTime(int seconds) {
		Date date = new Date(seconds * 1000);
		return (seconds >= 3600 ? (int) Math.floor(seconds / 3600.0) + ":" : "") + new SimpleDateFormat("mm:ss").format(date);
	}

	public static void invokeLater(Runnable task) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Plugin.INSTANCE, task);
	}

	public static void invokeLater(Runnable task, long delay) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Plugin.INSTANCE, task, delay);
	}

	@SuppressWarnings("deprecated")
	public static void asyncInvokeLater(Runnable task, long delay) {
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(Plugin.INSTANCE, task, delay);
	}

	public static boolean isSecUpdated() {
		return tick == 0;
	}

	public static boolean isMinUpdated() {
		return sec == 0 && isSecUpdated();
	}

}
