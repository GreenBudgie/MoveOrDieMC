package ru.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ru.util.ParticleUtils;
import ru.util.TaskManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomBlockSpringboard extends CustomBlock {

	private Map<Location, Integer> resetDelay = new HashMap<>();

	@Override
	public Material getType() {
		return Material.PISTON;
	}

	public boolean useFace() {
		return true;
	}

	@Override
	public void update() {
		Iterator<Location> iter = resetDelay.keySet().iterator();
		while(iter.hasNext()) {
			Location location = iter.next();
			int ticks = resetDelay.get(location);
			if(ticks <= 0) {
				iter.remove();
			} else {
				resetDelay.put(location, ticks - 1);
			}
		}
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		Piston piston = (Piston) block.getBlockData();
		if(piston.getFacing() != face) return false;
		if(resetDelay.containsKey(block.getLocation())) return true;
		Vector direction = face.getDirection();
		Vector playerVelocity = player.getVelocity();
		if(direction.getX() != 0) playerVelocity.setX(0);
		if(direction.getY() != 0) playerVelocity.setY(0);
		if(direction.getZ() != 0) playerVelocity.setZ(0);
		playerVelocity.add(direction.multiply(1.3));
		player.setVelocity(playerVelocity);
		TaskManager.invokeLater(() -> player.setVelocity(playerVelocity), 1L);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 1.5F);
		ParticleUtils.createParticlesAround(player, Particle.CLOUD, null, 8);
		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y <= 1; y++) {
				for(int z = -1; z <= 1; z++) {
					resetDelay.put(block.getLocation().add(x, y, z), 10);
				}
			}
		}
		return true;
	}

}
