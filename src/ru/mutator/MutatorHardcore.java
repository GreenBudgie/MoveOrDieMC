package ru.mutator;

import org.bukkit.Material;

public class MutatorHardcore extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.RED_DYE;
	}

	@Override
	public String getName() {
		return "�������";
	}

	@Override
	public String getDescription() {
		return "�� ������ � ��� ���� �������";
	}

}
