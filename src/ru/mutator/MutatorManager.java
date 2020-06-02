package ru.mutator;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class MutatorManager implements Listener {

	public static Mutator FORCED_MUTATOR = null;
	public static boolean FORCE_DISABLE = false;
	public static boolean FORCE_SELECTOR = false;
	protected static Mutator activeMutator = null;
	private static List<Mutator> mutators = new ArrayList<>();
	public static MutatorJumpBoost JUMP_BOOST = new MutatorJumpBoost();
	public static MutatorHyperSpeed HYPER_SPEED = new MutatorHyperSpeed();
	public static MutatorBombDrop BOMB_DROP = new MutatorBombDrop();
	public static MutatorDeathTouch DEATH_TOUCH = new MutatorDeathTouch();
	public static MutatorFlyOrDie FLY_OR_DIE = new MutatorFlyOrDie();
	public static MutatorBoost BOOST = new MutatorBoost();
	public static MutatorHardcore HARDCORE = new MutatorHardcore();
	public static MutatorKnockback KNOCKBACK = new MutatorKnockback();
	public static MutatorFallDamage FALL_DAMAGE = new MutatorFallDamage();

	public static void update() {
		if(activeMutator != null) {
			activeMutator.update();
		}
	}

	public static List<Mutator> getMutators() {
		return mutators;
	}

	public static Mutator getActiveMutator() {
		return activeMutator;
	}

	public static void deactivateMutator() {
		if(activeMutator != null) {
			if(activeMutator instanceof Listener) {
				HandlerList.unregisterAll((Listener) activeMutator);
			}
			activeMutator.onDeactivate();
			activeMutator = null;
		}
	}

}
