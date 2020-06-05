package ru.lobby.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ru.game.GameState;
import ru.game.MoveOrDie;
import ru.game.Rating;

public class SignRating extends LobbySign {

	public SignRating(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public void updateText() {
		clearText();
		Sign sign = getSign();
		sign.setLine(1, ChatColor.DARK_GREEN + "Рейтинг:");
		sign.setLine(2, Rating.ratingEnabled ? ChatColor.GREEN + "" + ChatColor.BOLD + "Включен" : ChatColor.RED + "" + ChatColor.BOLD + "Отключен");
		sign.update();
	}

	@Override
	public void onClick(Player player) {
		if(!GameState.isPlaying()) {
			if(player.isOp()) {
				Rating.ratingEnabled = !Rating.ratingEnabled;
			}
		}
	}

}
