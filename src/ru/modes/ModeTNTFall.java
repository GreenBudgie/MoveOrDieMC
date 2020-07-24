package ru.modes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.map.GameMap;
import ru.util.MathUtils;
import ru.util.Region;

import java.util.HashMap;
import java.util.Map;

public class ModeTNTFall extends Mode implements Listener {

	private Map<GameMap, Region> tntSpawnRegion = new HashMap<>();
	private final int maxDelay = 35, minDelay = 8, maxCount = 7, minCount = 2;
	private float count, delay, prevDelay;

	@Override
	public String getName() {
		return "Небесные Взрывы";
	}

	@Override
	public String getDescription() {
		return "С неба падает динамит! Старайся не взорваться как можно дольше";
	}

	@Override
	public Material getItemToShow() {
		return Material.TNT;
	}

	@Override
	public String getID() {
		return "TNT_FALL";
	}

	@Override
	public void deserializeMapOptions(GameMap map, ConfigurationSection options) {
		Region region = Region.deserialize(options.getValues(false));
		tntSpawnRegion.put(map, region);
	}

	@Override
	public void onRoundStart() {
		delay = maxDelay;
		prevDelay = delay;
		count = minCount;
	}

	@Override
	public void update() {
		if(delay <= 0) {
			Region tntRegion = tntSpawnRegion.get(map);
			for(int i = 0; i < Math.floor(count); i++) {
				Location spawn = tntRegion.getRandomInsideBlockLocation();
				spawn.setWorld(WorldManager.getCurrentGameWorld());
				TNTPrimed tnt = (TNTPrimed) spawn.getWorld().spawnEntity(spawn.clone().add(0.5, 0.5, 0.5), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(MathUtils.randomRange(20, 80));
			}
			delay = Math.max(prevDelay - (float) MathUtils.randomRangeDouble(0.1, 0.25), minDelay);
			prevDelay = delay;
			count = Math.min(count + (float) MathUtils.randomRangeDouble(0.05, 0.15), maxCount);
		} else {
			delay -= 1;
		}
	}

	@EventHandler
	public void tntExplode(EntityExplodeEvent e) {
		e.setYield(e.getYield() / 2F);
	}

	@EventHandler
	public void tntExplode(BlockExplodeEvent e) {
		e.setYield(e.getYield() / 2F);
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
