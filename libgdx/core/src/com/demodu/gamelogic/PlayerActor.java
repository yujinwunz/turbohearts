package com.demodu.gamelogic;

import java.util.List;

/**
 * Created by yujinwunz on 18/03/2017.
 */

public interface PlayerActor {
	void getMove(ClientGameView clientGameView, MoveReporter reporter);

	void reportPlay(GameState.PlayerPosition position, Card card);
	void reportPass(GameState.PlayerPosition position, List<Card> cards);
	void reportCharge(GameState.PlayerPosition position, Card card);
	void reportTrickEnd(GameState.PlayerPosition position);
	void reportRoundEnd(int score, int leftScore, int acrossScore, int rightScore);
	void reportGameEnd(int score, int leftScore, int acrossScore, int rightScore);
}
