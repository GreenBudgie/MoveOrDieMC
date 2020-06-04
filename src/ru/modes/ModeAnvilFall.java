package ru.modes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.map.GameMap;
import ru.map.MapManager;
import ru.util.MathUtils;
import ru.util.Region;

import java.util.HashMap;
import java.util.Map;

public class ModeAnvilFall extends Mode implements Listener {

	private Map<GameMap, Region> anvilSpawnRegion = new HashMap<>();
	private final int maxDelay = 20, minDelay = 3, maxCount = 10, minCount = 2;
	private float count, delay, prevDelay;

	@Override
	public String getName() {
		return "Наковальнепад";
	}

	@Override
	public String getDescription() {
		return "С неба падают наковальни! Самое сложное - не запутаться в них. К тому же, они могут тебя расплющить.";
	}

	@Override
	public Material getItemToShow() {
		return Material.ANVIL;
	}

	@Override
	public String getID() {
		return "ANVIL_FALL";
	}

	@Override
	public void deserializeMapOptions(GameMap map, ConfigurationSection options) {
		Region region = Region.deserialize(options.getValues(false));
		anvilSpawnRegion.put(map, region);
	}

	@Override
	public void onRoundStart() {
		delay = maxDelay;
		prevDelay = delay;
		count = minCount;
	}

	private static void randomlyRotate(Block block) {
		Directional data = (Directional) block.getBlockData();
		data.setFacing(MathUtils.choose(BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH));
		block.setBlockData(data);
	}

	@Override
	public void update() {
		if(delay <= 0) {
			Region anvilRegion = anvilSpawnRegion.get(map);
			for(int i = 0; i < count; i++) {
				Location spawn = anvilRegion.getRandomInsideBlockLocation();
				spawn.setWorld(WorldManager.getCurrentGameWorld());
				Block block = spawn.getBlock();
				if(block.getType() != Material.ANVIL) {
					block.setType(Material.ANVIL);
					randomlyRotate(block);
				}
			}
			delay = Math.max(prevDelay - (float) MathUtils.randomRangeDouble(0.1, 0.3), minDelay);
			prevDelay = delay;
			count = Math.min(count + (float) MathUtils.randomRangeDouble(0.1, 0.3), maxCount);
		} else {
			delay -= 1;
		}
	}

	@EventHandler
	public void anvilDamage(EntityDamageEvent e) {
		if(e.getEntityType() == EntityType.PLAYER && e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
			Player player = (Player) e.getEntity();
			if(PlayerHandler.isPlaying(player)) {
				e.setDamage(100);
			}
		}
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
