package com.demodu.gamelogic;

import java.util.List;

/**
 * Created by yujinwunz on 18/03/2017.
 */

public interface PlayerActor {
	void getMove(ClientGameView clientGameView, MoveReporter reporter);

	void reportPlay(GameConductor.PlayerPosition position, Card card);
	void reportPass(GameConductor.PlayerPosition position, List<Card> cards);
	void reportCharge(GameConductor.PlayerPosition position, Card card);
	void reportTrickEnd(GameConductor.PlayerPosition position);
	void reportRoundEnd(int score, int leftScore, int acrossScore, int rightScore);
	void reportGameEnd(int score, int leftScore, int acrossScore, int rightScore);
}
