package ru.blocks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.util.Broadcaster;
import ru.util.ParticleUtils;
import ru.util.TaskManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomBlockAccelerator extends CustomBlock {

	private Map<Location, Integer> resetDelay = new HashMap<>();
	private Map<Player, Integer> fireDelay = new HashMap<>();
	private static int maxFireDelay = 8;

	@Override
	public Material getType() {
		return Material.MAGENTA_GLAZED_TERRACOTTA;
	}

	public boolean useFace() {
		return true;
	}

	@Override
	public void update() {
		Iterator<Location> resetIterator = resetDelay.keySet().iterator();
		while(resetIterator.hasNext()) {
			Location location = resetIterator.next();
			int ticks = resetDelay.get(location);
			if(ticks <= 0) {
				resetIterator.remove();
			} else {
				resetDelay.put(location, ticks - 1);
			}
		}
		Iterator<Player> fireIterator = fireDelay.keySet().iterator();
		while(fireIterator.hasNext()) {
			Player player = fireIterator.next();
			if(player == null || !player.isOnline()) {
				fireIterator.remove();
				continue;
			}
			int ticks = fireDelay.get(player);
			if(ticks <= 0) {
				fireIterator.remove();
			} else {
				ParticleUtils.createParticlesAround(player, Particle.REDSTONE, Color.fromRGB(230, 120, 230), (int) Math.ceil(((double) ticks / maxFireDelay) * 3));
				fireDelay.put(player, ticks - 1);
			}
		}
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		if(resetDelay.containsKey(block.getLocation())) return true;
		Directional directional = (Directional) block.getBlockData();
		BlockFace blockDirection = directional.getFacing();
		BlockFace arrowDirection = BlockFace.UP;
		switch(face) {
		case UP: arrowDirection = blockDirection.getOppositeFace(); break;
		case DOWN: arrowDirection = blockDirection; break;
		case SOUTH:
			switch(blockDirection) {
			case SOUTH: arrowDirection = BlockFace.WEST; break;
			case WEST: arrowDirection = BlockFace.DOWN; break;
			case NORTH: arrowDirection = BlockFace.EAST; break;
			case EAST: arrowDirection = BlockFace.UP; break;
			}
			break;
		case WEST:
			switch(blockDirection) {
			case SOUTH: arrowDirection = BlockFace.UP; break;
			case WEST: arrowDirection = BlockFace.NORTH; break;
			case NORTH: arrowDirection = BlockFace.DOWN; break;
			case EAST: arrowDirection = BlockFace.SOUTH; break;
			}
			break;
		case NORTH:
			switch(blockDirection) {
			case SOUTH: arrowDirection = BlockFace.WEST; break;
			case WEST: arrowDirection = BlockFace.UP; break;
			case NORTH: arrowDirection = BlockFace.EAST; break;
			case EAST: arrowDirection = BlockFace.DOWN; break;
			}
			break;
		case EAST:
			switch(blockDirection) {
			case SOUTH: arrowDirection = BlockFace.DOWN; break;
			case WEST: arrowDirection = BlockFace.NORTH; break;
			case NORTH: arrowDirection = BlockFace.UP; break;
			case EAST: arrowDirection = BlockFace.SOUTH; break;
			}
			break;
		}
		Vector velocity = arrowDirection.getDirection();
		velocity.multiply(1.4);
		velocity.add(face.getDirection().multiply(0.25));
		player.setVelocity(velocity);
		TaskManager.invokeLater(() -> player.setVelocity(velocity), 1L);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1F, 0.5F);
		ParticleUtils.createParticlesAround(player, Particle.CLOUD, null, 8);
		fireDelay.put(player, maxFireDelay);
		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y <= 1; y++) {
				for(int z = -1; z <= 1; z++) {
					resetDelay.put(block.getLocation().add(x, y, z), 7);
				}
			}
		}
		return true;
	}

}
