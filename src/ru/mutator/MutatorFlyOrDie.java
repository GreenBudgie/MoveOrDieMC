package ru.mutator;

import org.bukkit.Material;

public class MutatorFlyOrDie extends Mutator {

	@Override
	public Material getItemToShow() {
		return Material.PHANTOM_MEMBRANE;
	}

	@Override
	public String getName() {
		return "���� ��� ����";
	}

	@Override
	public String getDescription() {
		return "�������� �������������� ������ � �������";
	}

}
