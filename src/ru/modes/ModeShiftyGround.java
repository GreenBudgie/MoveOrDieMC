package ru.modes;

import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import ru.blocks.CustomBlockManager;
import ru.game.PlayerHandler;
import ru.util.Broadcaster;
import ru.util.MathUtils;
import ru.util.ParticleUtils;
import ru.util.Region;

import java.util.*;

public class ModeShiftyGround extends Mode {

	private final Material SAFE_TYPE = Material.CHISELED_QUARTZ_BLOCK;
	private final Material DANGER_TYPE_1 = Material.YELLOW_TERRACOTTA;
	private final Material DANGER_TYPE_2 = Material.ORANGE_TERRACOTTA;
	private final Material DANGER_TYPE_3 = Material.RED_TERRACOTTA;
	private final Material DEATH_TYPE = Material.AIR;
	private int safePlaces, phaseDelay, prevDangerDelay;
	private Set<Block> blocks;
	private Set<Block> dangerBlocks = new HashSet<>();
	private Phase phase = Phase.SAFE;

	private enum Phase {

		SAFE, DANGER, DEATH;

	}

	@Override
	public String getName() {
		return "Хитрый Пол";
	}

	@Override
	public String getDescription() {
		return "Почти все блоки исчезают. Пытайся успеть добежать до тех, которые остались нетронутыми!";
	}

	@Override
	public Material getItemToShow() {
		return Material.RED_TERRACOTTA;
	}

	@Override
	public String getID() {
		return "SHIFTY_GROUND";
	}

	@Override
	public void onRoundPrepare() {
		phase = Phase.SAFE;
		phaseDelay = 40;
		prevDangerDelay = 80;
		safePlaces = 3; //TODO (int) Math.ceil(PlayerHandler.getMDPlayers().size() / 1.5);
		blocks = map.getRegion().getBlocksInside();
		blocks.removeIf(block -> block.getType() != SAFE_TYPE);
	}

	private Set<Block> getSafeRegion(Location center) {
		Material centerType = center.getBlock().getType();
		Region region = new Region(center.clone().subtract(MathUtils.randomRange(1, 2), MathUtils.randomRange(1, 2), MathUtils.randomRange(1, 2)),
				center.clone().add(MathUtils.randomRange(1, 2), MathUtils.randomRange(1, 2), MathUtils.randomRange(1, 2)));
		Set<Block> blocks = new HashSet<>();
		for(Block block : region.getBlocksInside()) {
			if(block.getType() == centerType) {
				blocks.add(block);
			}
		}
		return blocks;
	}

	@Override
	public void update() {
		Broadcaster broadcaster = Broadcaster.each(PlayerHandler.getPlayers());
		phaseDelay--;
		switch(phase) {
		case SAFE:
			if(phaseDelay <= 0) {
				Set<Block> remaining = Sets.newHashSet(blocks);
				for(int i = 0; i < safePlaces; i++) {
					if(remaining.isEmpty()) break;
					Location safePlace = MathUtils.choose(remaining).getLocation();
					remaining.removeAll(getSafeRegion(safePlace));
				}
				for(Block block : remaining) {
					block.setType(DANGER_TYPE_1);
					dangerBlocks.add(block);
				}
				broadcaster.sound(Sound.ITEM_FIRECHARGE_USE, 1F, 0.8F);
				phaseDelay = Math.max(prevDangerDelay - MathUtils.randomRange(5, 9), 20);
				prevDangerDelay = phaseDelay;
				phase = Phase.DANGER;
			}
			break;
		case DANGER:
			if(phaseDelay <= 0) {
				for(Block block : dangerBlocks) {
					block.setType(DEATH_TYPE);
					ParticleUtils.createParticlesInside(block, Particle.FLAME, null, 2);
				}
				broadcaster.sound(Sound.BLOCK_ANVIL_LAND, 1F, 0.5F);
				dangerBlocks.clear();
				phaseDelay = 80;
				phase = Phase.DEATH;
			} else {
				if(phaseDelay == (int) (prevDangerDelay * 0.66)) {
					for(Block block : dangerBlocks) {
						block.setType(DANGER_TYPE_2);
					}
					broadcaster.sound(Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 0.8F);
				}
				if(phaseDelay == (int) (prevDangerDelay * 0.33)) {
					for(Block block : dangerBlocks) {
						block.setType(DANGER_TYPE_3);
					}
					broadcaster.sound(Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 0.5F);
				}
			}
			break;
		case DEATH:
			if(phaseDelay <= 0) {
				for(Block block : blocks) {
					if(block.getType() == DEATH_TYPE) {
						block.setType(SAFE_TYPE);
						ParticleUtils.createParticlesOutline(block, Particle.CLOUD, null, 2);
					}
				}
				broadcaster.sound(Sound.ENTITY_ENDER_DRAGON_FLAP, 1F, 1.5F);
				if(MathUtils.chance(50)) safePlaces = Math.max(safePlaces - 1, 1);
				phaseDelay = 40;
				phase = Phase.SAFE;
			}
			break;
		}
	}

	@Override
	public void onRoundEnd() {
		blocks.clear();
		dangerBlocks.clear();
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

	@Override
	public int getTime() {
		return -1;
	}

}
