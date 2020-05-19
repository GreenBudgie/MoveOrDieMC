package ru.blocks;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import ru.util.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlockManager {

	public static final List<CustomBlock> customBlocks = new ArrayList<>();
	public static final CustomBlockSpringboard SPRINGBOARD = new CustomBlockSpringboard();
	public static final CustomBlockDeath DEATH = new CustomBlockDeath();
	private static Map<Map.Entry<BoundingBox, CustomBlock>, Integer> delayedRegions = new HashMap<>();
	private static Map<Player, Location> prevLocs = new HashMap<>();

	public static void update() {
		for(Map.Entry<BoundingBox, CustomBlock> entry : delayedRegions.keySet()) {
			int ticks = delayedRegions.get(entry);
			if(ticks <= 0) {
				delayedRegions.remove(entry);
			} else {
				delayedRegions.replace(entry, ticks - 1);
			}
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			A:
			for(CustomBlock customBlock : customBlocks) {
				BoundingBox box = player.getBoundingBox();
				for(Map.Entry<BoundingBox, CustomBlock> entry : delayedRegions.keySet()) {
					if(entry.getValue() == customBlock && entry.getKey().overlaps(box)) {
						continue A;
					}
				}
				box.expand(0.1);

				Region region = new Region(box);
				region.setWorld(player.getWorld());
				B:
				for(Block currentBlock : region.getBlocksInside()) {
					if(customBlock.getType() == currentBlock.getType()) {
						if(customBlock.useFace()) {
							for(BlockFace face : new BlockFace[] {BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN}) {
								BoundingBox blockBox = currentBlock.getBoundingBox();
								if(currentBlock.getRelative(face).getType() == Material.AIR && blockBox.expand(face, 1)
										.overlaps(player.getBoundingBox().expand(-0.05))) {
									if(customBlock.onTouch(player, currentBlock, face)) {
										if(customBlock.getUseDelayTicks() > 0) {
											BoundingBox bb = BoundingBox
													.of(currentBlock.getLocation().subtract(1, 1, 1), currentBlock.getLocation().add(2, 2, 2));
											delayedRegions.put(Maps.immutableEntry(bb, customBlock), customBlock.getUseDelayTicks());
										}
										break B;
									}
								}
							}
						} else {
							if(customBlock.onTouch(player, currentBlock, null)) {
								if(customBlock.getUseDelayTicks() > 0) {
									BoundingBox bb = BoundingBox.of(currentBlock.getLocation().subtract(1.5, 1.5, 1.5).toVector(),
											currentBlock.getLocation().add(1.5, 1.5, 1.5).toVector());
									delayedRegions.put(Maps.immutableEntry(bb, customBlock), customBlock.getUseDelayTicks());
								}
								break;
							}
						}
					}
				}
			}
			prevLocs.put(player, player.getLocation());
		}
	}

}
