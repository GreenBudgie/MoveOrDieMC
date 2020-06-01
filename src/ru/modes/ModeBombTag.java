package ru.modes;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.game.MDPlayer;
import ru.game.PlayerHandler;
import ru.util.EntityUtils;
import ru.util.ItemUtils;
import ru.util.MathUtils;
import ru.util.ParticleUtils;

import java.util.stream.Collectors;

public class ModeBombTag extends Mode implements Listener {

	private Player tagged = null;
	private final int maxDelayToExplode = 240, maxDelayToPickNew = 40;
	private int delayToExplode = maxDelayToExplode, delayToPickNew = maxDelayToPickNew, countdown = 20, prevCountdown = countdown;
	private boolean deathByExplosion = false;
	private BossBar bombBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);

	@Override
	public String getName() {
		return "Bomb Tag";
	}

	@Override
	public String getDescription() {
		return "Одного из игроков минируют, и он должен отдать бомбу другому, пока она не взорвалась";
	}

	@Override
	public Material getItemToShow() {
		return Material.TNT;
	}

	@Override
	public String getID() {
		return "BOMB_TAG";
	}

	private ItemStack getBombItem() {
		return ItemUtils.builder(Material.TNT).withName(ChatColor.DARK_RED + "Ты заминирован!").withGlow().build();
	}

	private void updateBombBar() {
		bombBar.setProgress(MathUtils.clamp((double) delayToExplode / maxDelayToExplode, 0, 1));
		String title;
		if(tagged == null) {
			title = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Время до взрыва";
		} else {
			MDPlayer md = MDPlayer.fromPlayer(tagged);
			title = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Заминирован: " + ChatColor.RESET + md.getColor() + md.getNickname();
		}
		bombBar.setTitle(title);
	}

	private void resetCountdown() {
		countdown = (int) Math.floor(Math.sqrt(1 + 8 * maxDelayToExplode) / 2 - 0.5);
		prevCountdown = countdown;
	}

	public Player getTaggedPlayer() {
		return tagged;
	}

	@Override
	public void update() {
		updateBombBar();
		if(tagged != null) {
			countdown--;
			if(countdown <= 0) {
				tagged.getWorld().playSound(tagged.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1F, 2F);
				countdown = prevCountdown - 1;
				prevCountdown = countdown;
			}
		}
		if(delayToExplode > 0) {
			delayToExplode--;
		} else {
			if(tagged != null) {
				explode();
			}
			if(delayToPickNew > 0) {
				delayToPickNew--;
			} else {
				delayToExplode = maxDelayToExplode;
				giveBomb(MathUtils.choose(PlayerHandler.getAlive().stream().map(MDPlayer::getPlayer).collect(Collectors.toList())));
			}
		}
	}

	private void explode() {
		if(tagged == null) return;
		deathByExplosion = true;
		tagged.setHealth(0);
		tagged.getWorld().createExplosion(tagged.getLocation(), 0);
		ParticleUtils.createParticlesInsideSphere(EntityUtils.getEntityCenter(tagged), 2, Particle.EXPLOSION_HUGE, null, 10);
		delayToPickNew = maxDelayToPickNew;
		tagged = null;
		resetCountdown();
	}

	private void giveBomb(Player player) {
		if(tagged != null) {
			tagged.getInventory().clear();
		}
		PlayerInventory inv = player.getInventory();
		for(int i = 0; i < 9; i++) {
			inv.setItem(i, getBombItem());
		}
		inv.setHelmet(getBombItem());
		tagged = player;
	}

	@Override
	public void onRoundPrepare() {
		giveBomb(MathUtils.choose(PlayerHandler.getPlayers()));
		updateBombBar();
		PlayerHandler.getPlayers().forEach(bombBar::addPlayer);
		bombBar.setVisible(true);
		bombBar.setProgress(1);
		delayToExplode = maxDelayToExplode;
		resetCountdown();
}

	@Override
	public void onRoundPreEnd() {
		tagged = null;
		PlayerHandler.getPlayers().forEach(player -> player.getInventory().clear());
	}

	@Override
	public void onRoundEnd() {
		bombBar.removeAll();
		bombBar.setVisible(false);
	}

	@Override
	public void onPlayerDeath(MDPlayer mdPlayer) {
		Player player = mdPlayer.getPlayer();
		if(!deathByExplosion && tagged == player) {
			delayToExplode = 0;
			delayToPickNew = maxDelayToPickNew;
			tagged = null;
		}
		deathByExplosion = false;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player victim = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();
			if(tagged == damager) {
				giveBomb(victim);
				e.setDamage(0);
			} else {
				e.setCancelled(true);
			}
		}
	}

	@Override
	public int getTime() {
		return -1;
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
	public boolean allowBlockBreaking() {
		return false;
	}

	@Override
	public boolean useSurvivalGameMode() {
		return false;
	}

	@Override
	public boolean allowPVP() {
		return true;
	}

}
