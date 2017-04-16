package com.demodu.turbohearts.gamelogic;

import java.util.List;

/**
 * Created by yujinwunz on 18/03/2017.
 */

public interface PlayerActor {
	void getMove(ClientGameView clientGameView, MoveReporter reporter);

	void reportPlay(com.demodu.turbohearts.gamelogic.GameConductor.PlayerPosition position, Card card);
	void reportPass(com.demodu.turbohearts.gamelogic.GameConductor.PlayerPosition position, List<Card> cards);
	void reportCharge(com.demodu.turbohearts.gamelogic.GameConductor.PlayerPosition position, Card card);
	void reportTrickEnd(com.demodu.turbohearts.gamelogic.GameConductor.PlayerPosition position);
	void reportRoundEnd(int score, int leftScore, int acrossScore, int rightScore);
	void reportGameEnd(int score, int leftScore, int acrossScore, int rightScore);
}
