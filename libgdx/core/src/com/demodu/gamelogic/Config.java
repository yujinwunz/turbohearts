package com.demodu.gamelogic;

/**
 * Created by yujinwunz on 18/03/2017.
 */

public class Config {
	public GameEndConditionType gameEndConditionType;
	public int gameEndConditionThreshold;
	public GameType gameType;

	public boolean JackOfDiamonds;
	public boolean QueenBreaks;
	public boolean ShootersChoice;

	public Config(
			GameEndConditionType gameEndConditionType,
			int gameEndConditionThreshold,
			GameType gameType,
			boolean jackOfDiamonds,
			boolean queenBreaks,
			boolean shootersChoice
	) {
		this.gameEndConditionType = gameEndConditionType;
		this.gameEndConditionThreshold = gameEndConditionThreshold;
		this.gameType = gameType;
		JackOfDiamonds = jackOfDiamonds;
		QueenBreaks = queenBreaks;
		ShootersChoice = shootersChoice;

	}

	public static Config standardGame =
			new Config(
					GameEndConditionType.Rounds,
					8,
					GameType.Turbohearts,
					false,
					false,
					false
			);

	public enum GameEndConditionType {
		Points,
		Rounds,
		Never
	}

	public enum GameType {
		Hearts,
		Turbohearts
	}
}

