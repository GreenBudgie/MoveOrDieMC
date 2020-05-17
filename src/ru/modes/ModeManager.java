package ru.modes;

import com.google.common.collect.Sets;
import ru.game.GameState;
import ru.util.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModeManager {

	private static Mode activeMode = null;
	private static Set<Mode> availableModes;
	private static List<Mode> modes = new ArrayList<>();
	public static ModeFight FIGHT = new ModeFight();

	public static List<Mode> getModes() {
		return modes;
	}

	public static Mode getByID(String ID) {
		return modes.stream().filter(mode -> mode.getID().equalsIgnoreCase(ID)).findFirst().orElse(null);
	}

	public static Mode getActiveMode() {
		return activeMode;
	}

	public static void update() {
		if(activeMode != null && activeMode.isActive()) {
			activeMode.update();
		}
	}

	public static void setup() {
		availableModes = Sets.newHashSet(modes);
	}

	public static void cleanup() {
		availableModes.clear();
	}

	public static Mode selectRandomMode() {
		if(availableModes.isEmpty()) {
			setup();
		}
		activeMode = MathUtils.choose(availableModes);
		availableModes.remove(activeMode);
		return activeMode;
	}

	public static void startNewRound() {
		selectRandomMode().start();
		GameState.disableTimer();
		GameState.GAME.set();
	}

}
