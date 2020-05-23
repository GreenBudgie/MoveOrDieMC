package ru.blocks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.util.*;

import java.util.*;
import java.util.stream.Stream;

public class CustomBlockTeleporter extends CustomBlock {

	private Map<Location, Integer> resetDelay = new HashMap<>();
	private final Material OFF_MATERIAL = Material.DARK_PRISMARINE;
	public static BiMap<Location, Location> links = HashBiMap.create();

	@Override
	public Material getType() {
		return Material.SEA_LANTERN;
	}

	public boolean useFace() {
		return true;
	}

	public static void linkTeleporters(Location l1, Location l2) {
		links.put(l1, l2);
	}

	private static Location getClosestTeleporter(Location location) {
		Location closest = null;
		double min = Double.MAX_VALUE;
		Set<Location> allTeleporters = new HashSet<>();
		allTeleporters.addAll(links.values());
		allTeleporters.addAll(links.inverse().values());
		for(Location currentLocation : allTeleporters) {
			Location cloned = currentLocation.clone();
			cloned.setWorld(location.getWorld());
			double distance = location.distanceSquared(cloned);
			if(distance < min) {
				closest = currentLocation;
				min = distance;
			}
		}
		return closest;
	}

	private static Location getLinkedTeleporter(Location location) {
		Location teleporter = links.get(location);
		if(teleporter == null) {
			teleporter = links.inverse().get(location);
		}
		return teleporter;
	}

	private void deactivateRegion(Location center) {
		for(int x = -2; x <= 2; x++) {
			for(int y = -2; y <= 2; y++) {
				for(int z = -2; z <= 2; z++) {
					Location current = center.clone().add(x, y, z);
					if(current.getBlock().getType() == getType()) {
						ParticleUtils.createParticlesOutline(current.getBlock(), Particle.REDSTONE, Color.PURPLE, 10);
						current.getBlock().setType(OFF_MATERIAL);
						resetDelay.put(current, 40);
					}
				}
			}
		}
	}

	@Override
	public void update() {
		Iterator<Location> iter = resetDelay.keySet().iterator();
		while(iter.hasNext()) {
			Location location = iter.next();
			int ticks = resetDelay.get(location);
			if(ticks <= 0) {
				ParticleUtils.createParticlesOutline(location.getBlock(), Particle.CLOUD, null, 4);
				location.getBlock().setType(getType());
				iter.remove();
			} else {
				resetDelay.put(location, ticks - 1);
			}
		}
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		if(resetDelay.containsKey(block.getLocation())) return true;
		Location teleportFrom = getClosestTeleporter(block.getLocation());
		if(teleportFrom == null) return true;
		Location teleportTo = getLinkedTeleporter(teleportFrom);
		if(teleportTo == null) return true;
		Location resultTo = teleportTo.clone();
		resultTo.setWorld(player.getWorld());
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1.5F);
		EntityUtils.teleportCentered(player, resultTo, true, true);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1.5F);

		Vector direction = face.getOppositeFace().getDirection();
		Vector playerDirection = player.getLocation().getDirection();
		playerDirection.multiply(0.2);
		if(direction.getX() != 0) playerDirection.setX(0);
		if(direction.getY() != 0) playerDirection.setY(0);
		if(direction.getZ() != 0) playerDirection.setZ(0);
		playerDirection.add(direction.multiply(0.2));
		player.setVelocity(playerDirection);

		deactivateRegion(block.getLocation());
		deactivateRegion(resultTo);
		return true;
	}

}
