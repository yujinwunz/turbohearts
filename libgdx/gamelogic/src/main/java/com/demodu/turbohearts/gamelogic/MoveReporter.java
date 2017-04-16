package com.demodu.turbohearts.gamelogic;

import java.util.List;

public abstract class MoveReporter {
	private boolean valid = true;

	public void reportMove(List<Card> move) throws InvalidMoveException {
		if (valid) {
			valid = false;
			reportMoveImpl(move);
		} else {
			throw new InvalidMoveException("Cannot report a move twice.");
		}
	}

	protected abstract void reportMoveImpl(List<Card> move);

	public class InvalidMoveException extends Exception {
		public InvalidMoveException(String message, Exception cause) {
			super(message, new Exception());
		}
		public InvalidMoveException(String message) {
			super(message);
		}
		public InvalidMoveException() {
			super();
		}
	}
}
