package ru.modes;

import java.util.ArrayList;
import java.util.List;

public class ModeManager {

	private static Mode activeMode = null;
	private static int countdown;
	public static List<Mode> modes = new ArrayList<>();
	public static ModeFight FIGHT = new ModeFight();

	public static Mode getByID(String ID) {
		return modes.stream().filter(mode -> mode.getID().equalsIgnoreCase(ID)).findFirst().orElse(null);
	}

	public static Mode getActiveMode() {
		return activeMode;
	}

	public static void update() {
		if(activeMode != null) {
			activeMode.update();
		}
	}



}
