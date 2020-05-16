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
	private static int timer = -1;

	public static int getTimer() {
		return timer;
	}

	public static void setTimer(int timer) {
		if(timer >= 0) {
			GameState.timer = timer;
		}
	}

	public static void disableTimer() {
		timer = -1;
	}

	/**
	 * Decreases the timer
	 * @return Whether the time has run out
	 */
	public static boolean updateTimer() {
		if(timer >= 0) {
			if(timer == 0) return true;
			timer--;
		}
		return false;
	}

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
