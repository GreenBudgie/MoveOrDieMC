package ru.lobby.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ru.game.GameState;
import ru.game.MoveOrDie;

public class SignPoints extends LobbySign {

	public SignPoints(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public void updateText() {
		clearText();
		Sign sign = getSign();
		sign.setLine(1, ChatColor.DARK_AQUA + "»гра до:");
		sign.setLine(2, ChatColor.AQUA + "" + ChatColor.BOLD + MoveOrDie.getScoreToWin());
		sign.update();
	}

	@Override
	public void onClick(Player player) {
		if(!GameState.isPlaying()) {
			if(player.isOp()) {
				MoveOrDie.cycleScoreToWin();
			}
		}
	}

}
