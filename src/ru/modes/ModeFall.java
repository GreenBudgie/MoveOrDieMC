package ru.modes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import ru.map.GameMap;
import ru.map.MapManager;
import ru.util.MathUtils;
import ru.util.ParticleUtils;
import ru.util.Region;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ModeFall extends Mode {

	private final Material CONVERT_TYPE = Material.CHISELED_QUARTZ_BLOCK;
	private final Material DESTROY_TYPE = Material.RED_TERRACOTTA;
	private Map<Block, Integer> delayToDestroy = new HashMap<>();
	private int delay = 0;
	private Set<Block> blocks;

	@Override
	public String getName() {
		return "Пропасть";
	}

	@Override
	public String getDescription() {
		return "Блоки начинают исчезать... Продержись на них как можно дольше";
	}

	@Override
	public Material getItemToShow() {
		return Material.PHANTOM_MEMBRANE;
	}

	@Override
	public String getID() {
		return "FALL";
	}

	@Override
	public void onRoundPrepare() {
		delay = 10;
		blocks = map.getRegion().getBlocksInside();
		blocks.removeIf(block -> block.getType() != CONVERT_TYPE);
	}

	private void convert(Block block) {
		ParticleUtils.createParticlesOutline(block, Particle.LAVA, null, 5);
		block.getWorld().playSound(block.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1F, (float) MathUtils.randomRangeDouble(1.2, 1.6));
		block.setType(DESTROY_TYPE);
		delayToDestroy.put(block, 25);
		blocks.remove(block);
	}

	private void destroy(Block block) {
		ParticleUtils.createParticlesInside(block, Particle.SMOKE_LARGE, null, 5);
		block.getWorld().playSound(block.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1F, (float) MathUtils.randomRangeDouble(1.5, 1.8));
		block.setType(Material.AIR);
	}

	@Override
	public void update() {
		Iterator<Block> iterator = delayToDestroy.keySet().iterator();
		while(iterator.hasNext()) {
			Block block = iterator.next();
			int delay = delayToDestroy.get(block);
			if(delay <= 0) {
				destroy(block);
				iterator.remove();
			} else {
				delayToDestroy.put(block, delay - 1);
			}
		}
		if(!blocks.isEmpty()) {
			delay--;
			if(delay <= 0) {
				int count = MathUtils.randomRange(2, 5);
				count = Math.min(count, blocks.size());
				for(int i = 0; i < count; i++) {
					Block convertBlock = MathUtils.choose(blocks);
					convert(convertBlock);
				}
				delay = MathUtils.randomRange(10, 20);
			}
		}
	}

	@Override
	public void onRoundEnd() {
		delayToDestroy.clear();
		blocks.clear();
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
