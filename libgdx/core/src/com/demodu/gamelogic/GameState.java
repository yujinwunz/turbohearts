package com.demodu.gamelogic;


public class GameState {
	private Player[] players;
	private int currentPlayer;
	private Phase phase;
	private Card[] table;
	private Config config;

	public GameState(Config config, PlayerActor[] actors, Card[] deck) {
		assert(actors.length == 4);
	}

	// Progress the game by one move.
	public void step() {
		// TODO
	}

	private static class Player {
		Card[] hand;
		Card[] taken;
		PlayerActor actor;
		private int pointsTotal;
	}

	public enum Phase {
		Ready,
		Passing,
		FirstRound,
		Charging,
		Playing,
		Finished
	}

	public enum Round {
		Left, Across, Right, NoPass
	}
}
