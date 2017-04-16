package com.demodu.turbohearts.service.game;

import com.demodu.turbohearts.api.messages.PollGameResponse;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.service.models.User;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LiveGameManager {
	Map<Integer, LiveGame> liveGames = new HashMap<Integer, LiveGame>();

	public int newGame(Session session, Set<User> users) {
		List<User> orderedUsers = new ArrayList<>(users);
		Collections.shuffle(orderedUsers);
		LiveGame liveGame = LiveGame.create(session, orderedUsers);
		liveGames.put(liveGame.getId(), liveGame);
		return liveGame.getId();
	}

	public void pollEvents(int gameId, int eventsAfter, User user, GameEventHandler handler)
			throws InvalidGameIdException, LiveGame.UserNotInGameException {
		if (!liveGames.containsKey(gameId)) {
			throw new InvalidGameIdException("Game id not found: " + gameId);
		}
		liveGames.get(gameId).pollEvents(user, eventsAfter, handler);
	}

	public void playMove(int gameId, User user, List<PollGameResponse.Card> cards)
			throws MoveReporter.InvalidMoveException
			, InvalidGameIdException
			, LiveGame.UserNotInGameException
	{
		if (!liveGames.containsKey(gameId)) {
			throw new InvalidGameIdException("Game id not found: " + gameId);
		}
		liveGames.get(gameId).playMove(user, cards);
	}

	public interface GameEventHandler {
		boolean handle(List<PollGameResponse.GameEvent> pendingEvents);
	}

	public class InvalidGameIdException extends Exception {
		public InvalidGameIdException() {
		}

		public InvalidGameIdException(String message) {
			super(message);
		}

		public InvalidGameIdException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
