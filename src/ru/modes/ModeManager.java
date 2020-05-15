package ru.modes;

import java.util.ArrayList;
import java.util.List;

public class ModeManager {

	public static List<Mode> modes = new ArrayList<>();

	public static Mode getByID(String ID) {
		return modes.stream().filter(mode -> mode.getID().equalsIgnoreCase(ID)).findFirst().orElse(null);
	}

}
