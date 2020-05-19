package ru.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public abstract class CustomBlock {

	public CustomBlock() {
		CustomBlockManager.customBlocks.add(this);
	}

	public boolean useFace() {
		return false;
	}

	public abstract Material getType();
	public abstract boolean onTouch(Player player, Block block, @Nullable BlockFace face);
	public abstract int getUseDelayTicks();

}
