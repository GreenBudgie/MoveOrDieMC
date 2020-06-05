package ru.start;

import de.slikey.effectlib.EffectManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.command.*;
import ru.game.GameState;
import ru.game.MoveOrDie;
import ru.game.Rating;
import ru.util.ParticleUtils;

public class Plugin extends JavaPlugin {

	public static Plugin INSTANCE;

	@Override
	public void onEnable() {
		INSTANCE = this;

		getCommand("gm").setExecutor(new CommandGM());
		getCommand("test").setExecutor(new CommandTest());
		getCommand("moveordie").setExecutor(new CommandMoveOrDie());
		getCommand("lobby").setExecutor(new CommandLobby());
		getCommand("rating").setExecutor(new CommandRating());
		getCommand("opstat").setExecutor(new CommandOpStat());


		ParticleUtils.effectManager = new EffectManager(this);
		MoveOrDie.init();
	}

	public void onDisable() {
		if(GameState.isPlaying()) MoveOrDie.endGame();
		Rating.save();
		ParticleUtils.effectManager.dispose();
	}

}
