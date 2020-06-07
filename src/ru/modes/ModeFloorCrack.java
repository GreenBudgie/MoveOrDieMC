package ru.modes;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.MathUtils;
import ru.util.ParticleUtils;

import java.util.*;

public class ModeFloorCrack extends Mode {

	private final Material POLISHED_TYPE = Material.POLISHED_ANDESITE;
	private final Material CRACKED_TYPE = Material.ANDESITE;
	private Set<Player> inAir = new HashSet<>();
	private Map<Block, Integer> destroyDelays = new HashMap<>();

	@Override
	public String getName() {
		return "Трещины";
	}

	@Override
	public String getDescription() {
		return "Блоки, по которым ты бежишь, трескаются, и вскоре вовсе ломаются. При прыжке и приземлении трескается еще больше блоков! Подрезай других и не упади сам.";
	}

	@Override
	public Material getItemToShow() {
		return Material.ANDESITE;
	}

	@Override
	public String getID() {
		return "FLOOR_CRACK";
	}

	private int getRandomDelay() {
		return MathUtils.randomRange(15, 25);
	}

	private boolean crackBlock(Block block) {
		if(block.getType() == POLISHED_TYPE) {
			ParticleUtils.createParticlesOutline(block, Particle.FLAME, null, 3);
			block.setType(CRACKED_TYPE);
			destroyDelays.put(block, getRandomDelay());
			return true;
		}
		return false;
	}

	private void destroyBlock(Block block) {
		if(block.getType() == CRACKED_TYPE) {
			ParticleUtils.createParticlesInside(block, Particle.SMOKE_NORMAL, null, 4);
			block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 0.8F, (float) MathUtils.randomRangeDouble(0.6, 0.8));
			block.setType(Material.AIR);
		}
	}

	private boolean jumpCrack(Location center) {
		Location[] locations = new Location[] {
				center.clone().add(0, 0, 1),
				center.clone().add(1, 0, 0),
				center.clone().add(0, 0, -1),
				center.clone().add(-1, 0, 0)
		};
		boolean crack = false;
		for(Location location : locations) {
			if(crack(location)) crack = true;
		}
		return crack;
	}

	private boolean crack(Location center) {
		Location[] locations = new Location[] {
				center,
				center.clone().add(0, 0, 1),
				center.clone().add(1, 0, 0),
				center.clone().add(0, 0, -1),
				center.clone().add(-1, 0, 0)
		};
		boolean crack = false;
		for(Location location : locations) {
			if(crackBlock(location.getBlock())) crack = true;
		}
		return crack;
	}

	@Override
	public void onRoundPreEnd() {
		inAir.clear();
		destroyDelays.clear();
	}

	@Override
	public void update() {
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			Player player = mdPlayer.getPlayer();
			if(!mdPlayer.isGhost()) {
				if(player.isOnGround()) {
					if(inAir.contains(player)) {
						if(jumpCrack(player.getLocation().subtract(0, 1, 0))) {
							player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_BREAK, 0.8F, 2F);
							ParticleUtils.createParticlesInsideSphere(player.getLocation(), 2, Particle.LAVA, null, 5);
						}
						inAir.remove(player);
					} else {
						if(crack(player.getLocation().subtract(0, 1, 0))) {
							player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_CRACK, 0.8F, 2F);
						}
					}
				} else {
					if(!inAir.contains(player)) {
						if(jumpCrack(player.getLocation().subtract(0, 1, 0))) {
							player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_BREAK, 0.8F, 1.5F);
							ParticleUtils.createParticlesInsideSphere(player.getLocation(), 2, Particle.LAVA, null, 5);
						}
						inAir.add(player);
					}
				}
			}
		}
		Iterator<Block> iterator = destroyDelays.keySet().iterator();
		while(iterator.hasNext()) {
			Block block = iterator.next();
			int delay = destroyDelays.get(block);
			if(delay <= 0) {
				destroyBlock(block);
				iterator.remove();
			} else {
				destroyDelays.put(block, delay - 1);
			}
		}
	}

	@Override
	public int getTime() {
		return -1;
	}

	@Override
	public boolean allowPVP() {
		return false;
	}

	@Override
	public boolean allowBlockBreaking() {
		return false;
	}

	@Override
	public boolean allowBlockPlacing() {
		return false;
	}

	@Override
	public boolean allowSuddenDeath() {
		return false;
	}

	@Override
	public boolean usePoints() {
		return false;
	}

	@Override
	public boolean useSurvivalGameMode() {
		return false;
	}

}
