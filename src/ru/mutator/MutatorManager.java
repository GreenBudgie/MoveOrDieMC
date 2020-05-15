package ru.mutator;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class MutatorManager implements Listener {

	public static Mutator activeMutator = null;
	public static List<Mutator> mutators = new ArrayList<>();

	public static void update() {
		if(activeMutator != null) {
			activeMutator.update();
		}
	}

}
