package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class MutatorBombDrop extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.TNT;
	}

	@Override
	public String getName() {
		return ChatColor.DARK_RED + "�������������";
	}

	@Override
	public String getDescription() {
		return "������ ������� ��� ���� �������";
	}

}
