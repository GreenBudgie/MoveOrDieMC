package ru.modes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.game.WorldManager;
import ru.map.GameMap;
import ru.mutator.MutatorManager;
import ru.util.ItemUtils;
import ru.util.MathUtils;
import ru.util.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModeCrossbower extends Mode implements Listener {

	private ListMultimap<GameMap, Region> regions = ArrayListMultimap.create();

	@Override
	public String getName() {
		return "Арбалетчик";
	}

	@Override
	public String getDescription() {
		return "Подбирай стрелы и убивай других игроков! Арбалет заряжается моментально при нажатии ПКМ.";
	}

	@Override
	public Material getItemToShow() {
		return Material.CROSSBOW;
	}

	@Override
	public String getID() {
		return "CROSSBOWER";
	}

	@Override
	public void onRoundPrepare() {
		for(MDPlayer mdPlayer : PlayerHandler.getMDPlayers()) {
			Player player = mdPlayer.getPlayer();
			PlayerInventory inv = player.getInventory();
			inv.setItem(0, ItemUtils.builder(Material.CROSSBOW).withEnchantments(new ItemUtils.Enchant(Enchantment.QUICK_CHARGE, 5),
					new ItemUtils.Enchant(Enchantment.PIERCING, 3)).unbreakable().build());
		}
	}

	@Override
	public void deserializeMapOptions(GameMap map, ConfigurationSection options) {
		for(String arrowRegionKey : options.getKeys(false)) {
			ConfigurationSection arrowRegionSection = options.getConfigurationSection(arrowRegionKey);
			Region region = Region.deserialize(arrowRegionSection.getValues(false));
			regions.put(map, region);
		}
	}

	@Override
	public void onRoundStart() {
		int arrowCount = Math.max(PlayerHandler.getMDPlayers().size() - 1, 1);
		for(int i = 0; i < arrowCount; i++) {
			Region region = new Region(MathUtils.choose(regions.get(map)));
			region.setWorld(WorldManager.getCurrentGameWorld());
			Location arrowSpawn = region.getRandomInsideAirBlockLocation();
			dropArrow(arrowSpawn);
		}
	}

	@Override
	public void onRoundPreEnd() {
		WorldManager.getCurrentGameWorld().getEntitiesByClass(Item.class).forEach(Item::remove);
	}

	@Override
	public void onPlayerDeath(MDPlayer mdPlayer) {
		Player player = mdPlayer.getPlayer();
		if(player.getKiller() != null) {
			Player killer = player.getKiller();
			MDPlayer killerMd = MDPlayer.fromPlayer(killer);
			if(killerMd != null) {
				killerMd.addPoint();
			}
		}
		if(hasArrow(player)) {
			dropArrow(player.getLocation());
		}
	}

	private boolean hasArrow(Player player) {
		ItemStack crossbow = player.getInventory().getItem(0);
		if(crossbow != null && crossbow.getType() == Material.CROSSBOW) {
			CrossbowMeta meta = (CrossbowMeta) crossbow.getItemMeta();
			if(meta != null && meta.hasChargedProjectiles()) return true;
		}
		return player.getInventory().contains(Material.ARROW);
	}

	private void dropArrow(Location location) {
		Item arrow = location.getWorld().dropItem(location.clone().add(0.5, 0.5, 0.5), new ItemStack(Material.ARROW));
		arrow.setPickupDelay(5);
		arrow.setGlowing(true);
		arrow.setInvulnerable(true);
		arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1.5F);
	}

	@EventHandler
	public void noArrowStack(ItemMergeEvent e) {
		if(e.getEntity().getItemStack().getType() == Material.ARROW) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void arrowPickup(EntityPickupItemEvent e) {
		if(e.getEntityType() == EntityType.PLAYER && e.getItem().getItemStack().getType() == Material.ARROW) {
			Player player = (Player) e.getEntity();
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null) {
				if(hasArrow(player) || mdPlayer.isGhost()) e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void arrowHit(ProjectileHitEvent e) {
		if(e.getHitEntity() instanceof Player) {
			Player player = (Player) e.getHitEntity();
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null && mdPlayer.isGhost()) {
				return;
			}
		}
		dropArrow(e.getEntity().getLocation());
		e.getEntity().remove();
	}

	@EventHandler
	public void arrowDamage(EntityDamageEvent e) {
		if(e.getEntityType() == EntityType.PLAYER) {
			Player player = (Player) e.getEntity();
			MDPlayer mdPlayer = MDPlayer.fromPlayer(player);
			if(mdPlayer != null) {
				if(e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
					e.setDamage(100);
				} else {
					if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !MutatorManager.KNOCKBACK.isActive()) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@Override
	public int getTime() {
		return 45;
	}

	@Override
	public boolean allowPVP() {
		return true;
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
		return true;
	}

	@Override
	public boolean usePoints() {
		return true;
	}

	@Override
	public boolean useSurvivalGameMode() {
		return false;
	}

}
