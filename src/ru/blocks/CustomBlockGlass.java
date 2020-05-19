package ru.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class CustomBlockGlass extends CustomBlock {

	@Override
	public Material getType() {
		return Material.WHITE_STAINED_GLASS;
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {

		return true;
	}

	@Override
	public int getUseDelayTicks() {
		return 0;
	}

}
