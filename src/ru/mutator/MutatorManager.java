package ru.mutator;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class MutatorManager implements Listener {

	protected static Mutator activeMutator = null;
	private static List<Mutator> mutators = new ArrayList<>();
	public static MutatorJumpBoost JUMP_BOOST = new MutatorJumpBoost();
	public static MutatorHyperSpeed HYPER_SPEED = new MutatorHyperSpeed();
	public static MutatorBombDrop BOMB_DROP = new MutatorBombDrop();

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
			activeMutator.onDeactivate();
			activeMutator = null;
		}
	}

}
