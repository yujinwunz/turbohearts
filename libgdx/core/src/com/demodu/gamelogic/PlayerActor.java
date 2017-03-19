package com.demodu.gamelogic;

import java.util.List;

/**
 * Created by yujinwunz on 18/03/2017.
 */

public interface PlayerActor {
	List<Card> getMove(ClientGameView clientGameView, MoveReporter reporter);
}
