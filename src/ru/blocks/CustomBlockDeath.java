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
import ru.modes.ModeDangerBuilder;
import ru.modes.ModeManager;
import ru.util.EntityUtils;
import ru.util.ParticleUtils;

import java.util.HashMap;
import java.util.Map;

public class CustomBlockDeath extends CustomBlock {

	@Override
	public Material getType() {
		return Material.REDSTONE_BLOCK;
	}

	@Override
	public boolean onTouch(Player player, Block block, BlockFace face) {
		if(PlayerHandler.isInLobby(player)) {
			EntityUtils.sendActionBarInfo(player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "—читай, ты погиб");
		} else if(GameState.GAME.isRunning()) {
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null) {
				if(!mdPlayer.isGhost()) {
					if(ModeManager.DANGER_BUILDER.isActive()) {
						Player killer = ModeManager.DANGER_BUILDER.getWhoPlacedBlock(block);
						if(killer != null) {
							MDPlayer mdKiller = MDPlayer.fromPlayer(killer);
							if(mdKiller != null) {
								mdKiller.addPoint();
							}
						}
					}
					player.setHealth(0);
					ParticleUtils.createParticlesInRange(block.getLocation().add(0.5, 0.5, 0.5), 2, Particle.SPELL_MOB, Color.RED, 40);
				}
			}
		}
		return true;
	}

}
