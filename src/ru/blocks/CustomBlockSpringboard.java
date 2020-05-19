package ru.blocks;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import ru.util.ParticleUtils;

public class CustomBlockSpringboard extends CustomBlock {

	@Override
	public Material getType() {
		return Material.PISTON;
	}

	public boolean useFace() {
		return true;
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		Piston piston = (Piston) block.getBlockData();
		if(piston.getFacing() != face) return false;
		Vector direction = face.getDirection();
		Vector playerVelocity = player.getVelocity();
		if(direction.getX() != 0) playerVelocity.setX(0);
		if(direction.getY() != 0) playerVelocity.setY(0);
		if(direction.getZ() != 0) playerVelocity.setZ(0);
		playerVelocity.add(direction.multiply(1.5));
		player.setVelocity(playerVelocity);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 1.5F);
		ParticleUtils.createParticlesAround(player, Particle.CLOUD, null, 8);
		return true;
	}

	@Override
	public int getUseDelayTicks() {
		return 10;
	}

}
