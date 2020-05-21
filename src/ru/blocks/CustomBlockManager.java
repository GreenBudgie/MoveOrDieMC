package ru.blocks;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import ru.util.Region;

import java.util.*;

public class CustomBlockManager {

	public static final List<CustomBlock> customBlocks = new ArrayList<>();
	public static final CustomBlockSpringboard SPRINGBOARD = new CustomBlockSpringboard();
	public static final CustomBlockDeath DEATH = new CustomBlockDeath();
	public static final CustomBlockGlass GLASS = new CustomBlockGlass();

	public static void update() {
		customBlocks.forEach(CustomBlock::update);
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getGameMode() == GameMode.SPECTATOR) continue;
			A:
			for(CustomBlock customBlock : customBlocks) {
				BoundingBox box = player.getBoundingBox();
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
										break B;
									}
								}
							}
						} else {
							if(customBlock.onTouch(player, currentBlock, null)) {
								break;
							}
						}
					}
				}
			}
		}
	}

}
