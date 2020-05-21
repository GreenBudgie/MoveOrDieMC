package ru.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.MathUtils;
import ru.util.ParticleUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomBlockGlass extends CustomBlock {

	private Map<Location, Integer> delayToHide = new HashMap<>();
	private Map<Location, Integer> delayToRespawn = new HashMap<>();
	private final Material HIDING_TYPE = Material.RED_STAINED_GLASS;

	@Override
	public Material getType() {
		return Material.WHITE_STAINED_GLASS;
	}

	@Override
	public void update() {
		Iterator<Location> hideIter = delayToHide.keySet().iterator();
		while(hideIter.hasNext()) {
			Location hideLoc = hideIter.next();
			int del = delayToHide.get(hideLoc);
			if(del <= 0) {
				hide(hideLoc);
				hideIter.remove();
			} else {
				delayToHide.put(hideLoc, del - 1);
			}
		}
		Iterator<Location> respawnIter = delayToRespawn.keySet().iterator();
		while(respawnIter.hasNext()) {
			Location respawnLoc = respawnIter.next();
			int del = delayToRespawn.get(respawnLoc);
			if(del <= 0) {
				respawn(respawnLoc);
				respawnIter.remove();
			} else {
				delayToRespawn.put(respawnLoc, del - 1);
			}
		}
	}

	public void respawnAll() {
		for(Location location : delayToHide.keySet()) {
			location.getBlock().setType(getType());
		}
		for(Location location : delayToRespawn.keySet()) {
			location.getBlock().setType(getType());
		}
		delayToRespawn.clear();
		delayToHide.clear();
	}

	public void respawn(Location location) {
		location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, 1F, (float) MathUtils.randomRangeDouble(1.5, 1.6));
		Block block = location.getBlock();
		ParticleUtils.createParticlesOutline(block, Particle.CLOUD, null, 4);
		block.setType(getType());
	}

	public void hide(Location location) {
		location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, 1F, (float) MathUtils.randomRangeDouble(0.5, 0.6));
		Block block = location.getBlock();
		if(block.getType() == HIDING_TYPE) {
			block.setType(Material.AIR);
			ParticleUtils.createParticlesInside(block, Particle.SMOKE_NORMAL, null, 4);
			delayToRespawn.put(location, MathUtils.randomRange(70, 80));
		}
	}

	public void markHiding(Location location) {
		if(delayToHide.containsKey(location) || delayToRespawn.containsKey(location)) return;
		Block block = location.getBlock();
		if(block.getType() == getType()) {
			block.setType(HIDING_TYPE);
			location.getWorld().playSound(location, Sound.BLOCK_SNOW_BREAK, 1F, 2F);
			delayToHide.put(location, MathUtils.randomRange(20, 30));
		}
	}

	public void markRegionToHide(Location center) {
		for(int x = -2; x <= 2; x++) {
			for(int y = -2; y <= 2; y++) {
				for(int z = -2; z <= 2; z++) {
					markHiding(center.clone().add(x, y, z));
				}
			}
		}
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		if(PlayerHandler.isInLobby(player)) {
			markRegionToHide(block.getLocation());
		} else {
			if(PlayerHandler.isPlaying(player)) {
				MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
				if(mdPlayer != null) {
					if(GameState.GAME.isRunning() && !mdPlayer.isGhost()) {
						markRegionToHide(block.getLocation());
					}
				}
			}
		}
		return true;
	}

}
