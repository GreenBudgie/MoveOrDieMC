package ru.game;

import java.util.stream.Stream;

public enum GameState {

	STOPPED, SETUP, ROUND_END, MUTATOR, GAME, FINALE;

	public void set() {
		setState(this);
	}

	public boolean isRunning() {
		return currentState == this;
	}

	private static GameState currentState = STOPPED;

	public static GameState getState() {
		return currentState;
	}

	public static boolean isState(GameState gameState) {
		return currentState == gameState;
	}

	public static boolean isState(GameState... gameStates) {
		return Stream.of(gameStates).anyMatch(state -> state == currentState);
	}

	public static boolean isPlaying() {
		return !isState(STOPPED);
	}

	public static void setState(GameState newState) {
		currentState = newState;
	}

}
