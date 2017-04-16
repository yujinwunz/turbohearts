package com.demodu.turbohearts.gamelogic;


public abstract class GameConductor {

	protected PlayerActor playerActor = null;

	public final void registerPlayer(PlayerActor playerActor) {
		if (this.playerActor != null) {
			throw new IllegalStateException("Player has already been registered");
		}
		this.playerActor = playerActor;
		onRegisterPlayer(playerActor);
	}

	public abstract void onRegisterPlayer(PlayerActor playerActor);

	public enum Phase {
		Ready,
		Passing,
		Charging,
		FirstRound,
		Playing,
		Finished
	}

	public enum Round {

		Left(1), Across(2), Right(3), NoPass(0);

		private int index;
		Round(int index) {
			this.index = index;
		}

		public int getPassIndex() {
			return index;
		}
	}

	public enum PlayerPosition {
		Self(0), Left(1), Across(2), Right(3);

		private int index;

		PlayerPosition(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public static PlayerPosition roundPassTo(Round r) {
			switch (r) {
				case Left:
					return Left;
				case Across:
					return Across;
				case Right:
					return Right;
				default:
					return null;
			}
		}

		public static PlayerPosition opposite(PlayerPosition p) {
			switch (p) {
				case Left:
					return Right;
				case Across:
					return Self;
				case Right:
					return Left;
				case Self:
					return Across;
				default:
					throw new IllegalArgumentException();
			}
		}

		public static PlayerPosition opposite(Round r) {
			switch (r) {
				case Left:
					return Right;
				case Across:
					return Across;
				case Right:
					return Left;
				case NoPass:
					return Self;
				default:
					throw new IllegalArgumentException();
			}
		}
	}
}
