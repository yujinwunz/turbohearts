package com.demodu.gamelogic;

import java.util.List;


/**
 * Created by yujinwunz on 19/03/2017.
 */

public abstract class MoveReporter {
	private boolean valid = true;

	public void reportMove(List<Card> move) {
		if (valid) {
			valid = false;
			reportMoveImpl(move);
		} else {
			throw new UnsupportedOperationException("Cannot report a move twice.");
		}
	}

	protected abstract void reportMoveImpl(List<Card> move);
}
