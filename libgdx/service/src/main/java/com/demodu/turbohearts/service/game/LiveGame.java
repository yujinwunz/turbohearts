package com.demodu.turbohearts.service.game;

import com.demodu.turbohearts.api.messages.PollGameResponse;
import com.demodu.turbohearts.api.messages.Util;
import com.demodu.turbohearts.gamelogic.GameConductor;
import com.demodu.turbohearts.gamelogic.LocalGameConductor;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.service.models.Game;
import com.demodu.turbohearts.service.models.Participation;
import com.demodu.turbohearts.service.models.User;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yujinwunz on 16/04/2017.
 */

public class LiveGame {
	private Game dbEntry;
	private GameConductor internalGameConductor;
	private Map<String, LivePlayerView> playerViews = new HashMap<>();

	private LiveGame(Game dbEntry) {
		this.dbEntry = dbEntry;
		for (Participation u : dbEntry.getParticipants()) {
			playerViews.put(u.getUser().getId(), new LivePlayerView());
		}
		internalGameConductor = new LocalGameConductor(
				playerViews.get(dbEntry.getParticipants().get(1).getUser().getId()),
				playerViews.get(dbEntry.getParticipants().get(2).getUser().getId()),
				playerViews.get(dbEntry.getParticipants().get(3).getUser().getId())
		);
		internalGameConductor.registerPlayer(
				playerViews.get(dbEntry.getParticipants().get(0).getUser().getId())
		);
	}

	public int getId() {
		return dbEntry.getId();
	}

	static LiveGame create(Session session, List<User> playersInOrder) {
		assert(playersInOrder.size() == 4);
		List<Participation> participations = new ArrayList<>();
		for (int i = 0; i < playersInOrder.size(); i++) {
			participations.add(new Participation(i, playersInOrder.get(i)));
		}
		Game newGame = new Game(participations);
		session.save(newGame);
		return new LiveGame(newGame);
	}

	public void pollEvents(
			User user,
			int afterEventNumber,
			LiveGameManager.GameEventHandler handler
	) throws UserNotInGameException {
		if (!playerViews.containsKey(user.getId())) {
			throw new UserNotInGameException(
					"User " + user.getUsername() + " is not in game " + dbEntry.getId());
		}
		playerViews.get(user.getId()).pollEvents(afterEventNumber, handler);
	}

	public void playMove(User user, List<PollGameResponse.Card> cards)
			throws MoveReporter.InvalidMoveException, UserNotInGameException {
		if (!playerViews.containsKey(user.getId())) {
			throw new UserNotInGameException(
					"User " + user.getUsername() + " is not in game " + dbEntry.getId());
		}
		playerViews.get(user.getId()).playMove(Util.toCore(cards));
	}

	public class UserNotInGameException extends Exception {
		public UserNotInGameException() {
		}

		public UserNotInGameException(String message) {
			super(message);
		}

		public UserNotInGameException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
