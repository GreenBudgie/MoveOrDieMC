package ru.mutator;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class MutatorJumpBoost extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.RABBIT_FOOT;
	}

	@Override
	public String getName() {
		return ChatColor.DARK_GREEN + "� �������!";
	}

	@Override
	public String getDescription() {
		return "������ ���������� ���� ��������";
	}

}
