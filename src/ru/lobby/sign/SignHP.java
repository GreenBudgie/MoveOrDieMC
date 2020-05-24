package ru.lobby.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ru.game.GameState;
import ru.game.MoveOrDie;
import ru.game.PlayerHandler;
import ru.util.EntityUtils;

public class SignHP extends LobbySign {

	public SignHP(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public void updateText() {
		clearText();
		Sign sign = getSign();
		sign.setLine(1, ChatColor.DARK_GREEN + "<Изменить");
		sign.setLine(2, ChatColor.DARK_GREEN + "отображение ХП>");
		sign.update();
	}

	@Override
	public void onClick(Player player) {
		PlayerHandler.cycleHPDisplay(player);
		PlayerHandler.HPDisplay display = PlayerHandler.getHPDisplay(player);
		EntityUtils.sendActionBarInfo(player, ChatColor.DARK_AQUA + "Выбран тип отображения: " + display.name);
	}

}
