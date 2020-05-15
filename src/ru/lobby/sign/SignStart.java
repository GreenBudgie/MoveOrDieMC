package ru.lobby.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ru.game.GameState;
import ru.game.MoveOrDie;
import ru.game.WorldManager;

public class SignStart extends LobbySign {

	public SignStart(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public void updateText() {
		clearText();
		Sign sign = getSign();
		if(GameState.isPlaying()) {
			sign.setLine(1, ChatColor.DARK_BLUE + "Игра идет...");
		} else {
			sign.setLine(1, (WorldManager.hasWorld() ? ChatColor.DARK_GREEN : ChatColor.DARK_GRAY) + "Начать игру");
		}
		sign.update();
	}

	@Override
	public void onClick(Player player) {
		if(!GameState.isPlaying()) {
			if(player.isOp()) {
				MoveOrDie.startGame();
			}
		}
	}

}
