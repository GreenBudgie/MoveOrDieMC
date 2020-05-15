package ru.lobby;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.game.PlayerHandler;
import ru.start.Plugin;
import ru.util.ItemUtils;
import ru.util.MathUtils;
import ru.util.ParticleUtils;
import ru.util.WorldUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LobbyEntertainmentHandler implements Listener {

	private static Map<Player, Jukebox> lastUsedJukebox = new HashMap<>();
	private static Map<Player, Location> lastUsedNoteBlock = new HashMap<>();
	private static Set<LobbyChair> chairs = new HashSet<>();
	private static List<Material> pottedMaterials = new ArrayList<>();
	private static Map<Material, String> noteBlockInstruments = new HashMap<>();

	public static void init() {
		Bukkit.getPluginManager().registerEvents(new LobbyEntertainmentHandler(), Plugin.INSTANCE);
		Bukkit.getPluginManager().registerEvents(new LobbyProtectionListener(), Plugin.INSTANCE);
		pottedMaterials = getPottedMaterials();
		noteBlockInstruments = getNoteBlockInstruments();
	}

	private static boolean isInLobby(Player p) {
		return PlayerHandler.isInLobby(p);
	}

	private static Map<Material, String> getNoteBlockInstruments() {
		Map<Material, String> map = new HashMap<>();
		map.put(Material.DARK_OAK_PLANKS, "���-������");
		map.put(Material.RED_SAND, "����� �������");
		map.put(Material.BLUE_STAINED_GLASS, "�������");
		map.put(Material.OBSIDIAN, "������� �������");
		map.put(Material.BONE_BLOCK, "��������");
		map.put(Material.GOLD_BLOCK, "����������");
		map.put(Material.CLAY, "������");
		map.put(Material.PACKED_ICE, "�������");
		map.put(Material.BROWN_WOOL, "������");
		map.put(Material.LAPIS_BLOCK, "�������");
		map.put(Material.IRON_BLOCK, "�������� ��������");
		map.put(Material.SOUL_SAND, "������� �����������");
		map.put(Material.PUMPKIN, "���������");
		map.put(Material.EMERALD_BLOCK, "��������");
		map.put(Material.HAY_BLOCK, "������");
		map.put(Material.GLOWSTONE, "������� ����");
		return map;
	}

	public static void removeChair(Player p) {
		LobbyChair chair = getChair(p);
		chair.getSittingOn().remove();
		chairs.remove(chair);
	}

	public static boolean isSitting(Player p) {
		return chairs.stream().anyMatch(chair -> chair.getSittingPlayer() == p);
	}

	public static LobbyChair getChair(Player p) {
		return chairs.stream().filter(chair -> chair.getSittingPlayer() == p).findFirst().orElse(null);
	}

	public static boolean hasChairOn(Location l) {
		return chairs.stream().anyMatch(chair -> WorldUtils.compareIntegerLocations(l, chair.getLocation()));
	}

	private static List<Material> getPottedMaterials() {
		return Stream.of(Material.values()).filter(type -> type.name().startsWith("POTTED")).collect(Collectors.toList());
	}

	private static boolean isPotted(Material type) {
		return getPottedMaterials().contains(type);
	}

	@EventHandler
	public void invClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(isInLobby(p)) {
			ItemStack item = e.getCurrentItem();
			if(e.getView().getTitle().equalsIgnoreCase(ChatColor.YELLOW + "������ �����")) {
				Jukebox jukebox = lastUsedJukebox.get(p);
				if(item != null && item.getType() != Material.AIR) {
					jukebox.setRecord(item);
					jukebox.setPlaying(item.getType());
					jukebox.update();
					ParticleUtils.createParticle(jukebox.getLocation().clone().add(0.5, 1.2, 0.5), Particle.NOTE, null);
					p.closeInventory();
				}
				e.setCancelled(true);
			}
			if(e.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "������ ����������")) {
				if(item != null && item.getType() != Material.AIR) {
					lastUsedNoteBlock.get(p).getBlock().setType(item.getType());
					p.closeInventory();
				}
			}
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(isInLobby(p)) {
			Block block = e.getClickedBlock();
			if(e.getAction() == Action.PHYSICAL && block.getType() == Material.STONE_PRESSURE_PLATE) {
				for(PotionEffectType ef : new PotionEffectType[] {PotionEffectType.GLOWING, PotionEffectType.SPEED, PotionEffectType.JUMP}) {
					p.addPotionEffect(new PotionEffect(ef, 600, 4, false, false));
					LobbyParkourHandler.stopPassing(p);
				}
			}
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.JUKEBOX && e.getHand() == EquipmentSlot.HAND) {
				Jukebox jukebox = (Jukebox) e.getClickedBlock().getState();
				if(jukebox.isPlaying()) {
					ParticleUtils.createParticle(jukebox.getLocation().clone().add(0.5, 1.2, 0.5), Particle.SMOKE_LARGE, null);
					jukebox.getWorld().playSound(jukebox.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 0.5F, 0.5F);
					jukebox.setPlaying(null);
					jukebox.setRecord(null);
					jukebox.update();
				} else {
					lastUsedJukebox.put(p, jukebox);
					Inventory inv = Bukkit.createInventory(p, 18, ChatColor.YELLOW + "������ �����");
					inv.addItem(Stream.of(Material.values()).filter(Material::isRecord).map(ItemStack::new).toArray(ItemStack[]::new));
					p.openInventory(inv);
				}
				e.setCancelled(true);
			}
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.NOTE_BLOCK && e.getHand() == EquipmentSlot.HAND && p.isSneaking()) {
				Inventory inv = Bukkit.createInventory(p, 18, ChatColor.GOLD + "������ ����������");
				inv.addItem(noteBlockInstruments.keySet().stream().map(type -> ItemUtils.setName(new ItemStack(type), ChatColor.LIGHT_PURPLE + noteBlockInstruments.get(type)))
						.toArray(ItemStack[]::new));
				p.openInventory(inv);
				lastUsedNoteBlock.put(p, e.getClickedBlock().getLocation().clone().add(0, -1, 0));
				e.setCancelled(true);
			}
			if(e.getAction() == Action.LEFT_CLICK_BLOCK && block.getType() == Material.NOTE_BLOCK && e.getHand() == EquipmentSlot.HAND
					&& p.getGameMode() == GameMode.ADVENTURE) {
				NoteBlock note = (NoteBlock) block.getBlockData();
				p.playNote(block.getLocation(), note.getInstrument(), note.getNote());
			}
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.SPRUCE_STAIRS && e.getHand() == EquipmentSlot.HAND) {
				if(!hasChairOn(block.getLocation())) {
					LobbyChair chair = new LobbyChair(p, block.getLocation().clone().add(0.5, 0, 0.5));
					chairs.add(chair);
				}
			}
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && (isPotted(block.getType()) || block.getType() == Material.FLOWER_POT) && e.getHand() == EquipmentSlot.HAND) {
				block.setType(MathUtils.choose(pottedMaterials));
				e.setCancelled(true);
			}
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.OAK_BUTTON && e.getHand() == EquipmentSlot.HAND) {
				p.getWorld().playSound(p.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1F, 1.5F);
				p.teleport(new Location(p.getWorld(), 7.5, 165, 16.5));
				p.getWorld().playSound(p.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1F, 1.5F);
			}
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.DARK_OAK_BUTTON && e.getHand() == EquipmentSlot.HAND) {
				p.getWorld().playSound(p.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1F, 1.5F);
				p.teleport(new Location(p.getWorld(), -15.5, 63, 54));
				p.getWorld().playSound(p.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1F, 1.5F);
			}
		}
	}

	private ItemStack getRandomFirework() {
		ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
		FireworkMeta meta = (FireworkMeta) item.getItemMeta();
		meta.setPower(MathUtils.randomRange(1, 3));
		int effects = MathUtils.randomRange(1, 3);
		for(int i = 0; i < effects; i++) {
			FireworkEffect.Builder builder = FireworkEffect.builder();
			builder.flicker(MathUtils.chance(30));
			builder.trail(MathUtils.chance(30));
			builder.with(MathUtils.choose(FireworkEffect.Type.values()));
			int colorsBase = MathUtils.randomRange(1, 3);
			int colorsFade = MathUtils.randomRange(1, 3);
			Color[] availableColors = new Color[] {Color.RED, Color.BLUE, Color.GREEN, Color.BLACK, Color.WHITE, Color.AQUA, Color.YELLOW, Color.FUCHSIA, Color.LIME,
					Color.ORANGE, Color.PURPLE, Color.TEAL, Color.NAVY, Color.OLIVE};
			for(int j = 0; j < colorsBase; j++) {
				builder.withColor(MathUtils.choose(availableColors));
			}
			for(int j = 0; j < colorsFade; j++) {
				builder.withFade(MathUtils.choose(availableColors));
			}
			meta.addEffect(builder.build());
		}
		item.setItemMeta(meta);
		return item;
	}

	@EventHandler
	public void tp(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		if(isInLobby(p)) {
			if(isSitting(p)) {
				removeChair(p);
			}
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(isInLobby(p)) {
			if(isSitting(p)) {
				removeChair(p);
			}
		}
	}

	@EventHandler
	public void itemFrameInteract(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		Entity ent = e.getRightClicked();
		if(ent instanceof ItemFrame && isInLobby(p)) {
			ItemFrame frame = (ItemFrame) ent;
			ItemStack item = frame.getItem();
			if(item.getType() == Material.CLOCK) {
				Rotation rot = frame.getRotation();
				frame.getWorld().setTime((rot.ordinal() + 3) * 3000);
			} else if(item.getType() == Material.FIREWORK_ROCKET) {
				PlayerInventory inv = p.getInventory();
				if(Stream.of(inv.getContents()).filter(Objects::nonNull).mapToInt(ItemStack::getAmount).sum() < 9) {
					p.getInventory().addItem(getRandomFirework());
				}
			} else {
				if(p.getGameMode() != GameMode.CREATIVE) {
					e.setCancelled(true);
				}
			}
		}
	}

}
