package ru.blocks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.game.GameState;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.EntityUtils;
import ru.util.ParticleUtils;

public class CustomBlockDeath extends CustomBlock {

	@Override
	public Material getType() {
		return Material.REDSTONE_BLOCK;
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		if(PlayerHandler.isInLobby(player)) {
			EntityUtils.sendActionBarInfo(player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "������, �� �����");
		} else if(GameState.GAME.isRunning()) {
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null) {
				if(!mdPlayer.isGhost()) {
					player.setHealth(0);
					ParticleUtils.createParticlesInRange(block.getLocation().add(0.5, 0.5, 0.5), 2, Particle.REDSTONE, Color.RED, 40);
				}
			}
		}
		return true;
	}

	@Override
	public int getUseDelayTicks() {
		return 0;
	}

}
