package ru.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import ru.map.MapFinale;
import ru.util.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GameFinaleManager {

	private static Set<Material> floorWool = Sets
			.newHashSet(Material.RED_WOOL, Material.GREEN_WOOL, Material.ORANGE_WOOL, Material.YELLOW_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL);
	private static MDPlayer winner = null;

	public static void start(MDPlayer winner) {
		GameFinaleManager.winner = winner;
		List<Location> locations = Lists.newArrayList(MapFinale.getSpawns());
		Collections.shuffle(locations);
		EntityUtils.teleport(winner.getPlayer(), MapFinale.getWinnerSpawn(), true, true);
		int c = 0;
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			Player player = mdPlayer.getPlayer();
			mdPlayer.resurrect();
			player.sendTitle(winner.getColor() + winner.getNickname(), ChatColor.AQUA + "" + ChatColor.BOLD + "Выиграл!", 10, 60, 20);
			PlayerHandler.resetNoEffects(player);
			player.setGameMode(GameMode.ADVENTURE);
			if(mdPlayer != winner) {
				EntityUtils.teleportCentered(player, locations.get(c++), true, true);
			}
			mdPlayer.getPlayer().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1F, 1F);
		}
		GameState.setTimer(15);
		GameState.FINALE.set();
	}

	public static void update() {
		if(TaskManager.isSecUpdated()) {
			if(GameState.updateTimer()) {
				MoveOrDie.endGame();
			} else {
				Region floor = MapFinale.getFloor();
				for(Block block : floor.getBlocksInside()) {
					block.setType(MathUtils.choose(floorWool));
				}
				Location fireworkLoc = floor.getRandomInsideLocation().add(0, 3, 0);
				Firework firework = (Firework) fireworkLoc.getWorld().spawnEntity(fireworkLoc, EntityType.FIREWORK);
				FireworkMeta meta = firework.getFireworkMeta();
				meta.setPower(0);
				meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(ParticleUtils.toColor(winner.getColor())).build());
				firework.setFireworkMeta(meta);
			}
		}
		if(TaskManager.ticksPassed(6)) {
			for(Player player : PlayerHandler.getPlayers()) {
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.8F, TaskManager.ticksPassed(12) ? 0.8F : 1.2F);
			}
		}
		if(TaskManager.ticksPassed(12)) {
			for(Player player : PlayerHandler.getPlayers()) {
					player.playNote(player.getLocation(), Instrument.GUITAR, Note.natural(1, MathUtils.choose(Note.Tone.C, Note.Tone.D, Note.Tone.E)));
			}
		}
	}

}
