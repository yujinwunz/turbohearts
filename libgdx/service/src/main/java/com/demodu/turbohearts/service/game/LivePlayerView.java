package com.demodu.turbohearts.service.game;

import com.demodu.turbohearts.api.messages.ImmutableGameEvent;
import com.demodu.turbohearts.api.messages.Util;
import com.demodu.turbohearts.gamelogic.Card;
import com.demodu.turbohearts.gamelogic.ClientGameView;
import com.demodu.turbohearts.gamelogic.GameConductor;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.gamelogic.PlayerActor;
import com.demodu.turbohearts.api.messages.PollGameResponse;
import com.demodu.turbohearts.service.events.Event;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LivePlayerView implements PlayerActor {
	private List<PollGameResponse.GameEvent> pastEvents = new ArrayList<>();
	private List<LiveGameManager.GameEventHandler> handlers = new ArrayList<>();
	private MoveReporter currentMoveReporter = null;
	private int currentEventNumber = 0;

	private synchronized List<PollGameResponse.GameEvent> getPendingEventsAfter(int eventNumber) {
		List<PollGameResponse.GameEvent> ret = new ArrayList<>();
		for (PollGameResponse.GameEvent e : pastEvents) {
			if (e.getEventNumber() > eventNumber) {
				ret.add(e);
			}
		}
		return ret;
	}

	private synchronized void onNewEvent(PollGameResponse.GameEvent event) {
		// clients resuming from a game doesn't need to know play by play specifics of previous
		// rounds to reconstruct the current round.
		try {
			System.out.println("Got new game event " + Event.objectMapper.writeValueAsString(event));
		} catch (JsonProcessingException ex) {
			System.out.println("Couldn't print new event " + event);
		}
		if (event.getEventType() == PollGameResponse.EventType.GameEnd ||
				event.getEventType() == PollGameResponse.EventType.RoundEnd) {
			while (!pastEvents.isEmpty()
					&& pastEvents.get(pastEvents.size()-1).getEventType() != PollGameResponse.EventType.GameEnd
					&& pastEvents.get(pastEvents.size()-1).getEventType() != PollGameResponse.EventType.RoundEnd) {
				pastEvents.remove(pastEvents.size()-1);
			}
		}
		pastEvents.add(event);
		for (int i = handlers.size()-1; i >= 0; i--) {
			if (!handlers.get(i).handle(Collections.singletonList(event))) {
				handlers.remove(i);
			}
		}
	}

	public synchronized void pollEvents(
			int afterEventNumber,
			LiveGameManager.GameEventHandler handler
	) {
		handlers.add(handler);
		List<PollGameResponse.GameEvent> res = getPendingEventsAfter(afterEventNumber);
		if (!res.isEmpty()) {
			if (!handler.handle(res)) {
				handlers.remove(handlers.size()-1);
			}
		}
	}

	public synchronized void playMove(List<Card> move, int lastEventNumber)
			throws MoveReporter.InvalidMoveException {
		if (lastEventNumber < currentEventNumber && currentMoveReporter == null) {
			return;
		}
		if (lastEventNumber > currentEventNumber) {
			throw new MoveReporter.InvalidMoveException("Version number is invalid for this game");
		}
		if (currentMoveReporter == null) {
			throw new MoveReporter.InvalidMoveException("Not expecting a move right now");
		}
		incrementEventNumber();
		MoveReporter temp = currentMoveReporter;
		currentMoveReporter = null;

		temp.reportMove(move);
	}

	@Override
	public synchronized void getMove(ClientGameView clientGameView, MoveReporter reporter) {
		currentMoveReporter = reporter;
		onNewEvent(ImmutableGameEvent
				.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.MoveRequested)
				.gameView(Util.toApi(clientGameView))
				.build()
		);
	}


	@Override
	public synchronized void reportPlay(GameConductor.PlayerPosition position, Card card) {
		onNewEvent(ImmutableGameEvent
			.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.PlayCard)
				.addCards(Util.toApi(card))
				.position(Util.toApi(position))
				.build()
		);
	}

	@Override
	public synchronized void reportPass(GameConductor.PlayerPosition position, List<Card> cards) {
		onNewEvent(ImmutableGameEvent
				.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.PassCards)
				.addAllCards(Util.toApi(cards))
				.position(Util.toApi(position))
				.build()
		);
	}

	@Override
	public synchronized void reportCharge(GameConductor.PlayerPosition position, Card card) {
		onNewEvent(ImmutableGameEvent
				.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.ChargeCard)
				.addCards(Util.toApi(card))
				.position(Util.toApi(position))
				.build()
		);
	}

	@Override
	public synchronized void reportTrickEnd(GameConductor.PlayerPosition position) {
		onNewEvent(ImmutableGameEvent
				.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.TrickEnd)
				.position(Util.toApi(position))
				.build()
		);
	}

	@Override
	public synchronized void reportRoundEnd(int score, int leftScore, int acrossScore, int rightScore) {
		onNewEvent(ImmutableGameEvent
				.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.RoundEnd)
				.selfScore(score)
				.leftScore(leftScore)
				.acrossScore(acrossScore)
				.rightScore(rightScore)
				.build()
		);
	}

	@Override
	public synchronized void reportGameEnd(int score, int leftScore, int acrossScore, int rightScore) {
		onNewEvent(ImmutableGameEvent
				.builder()
				.eventNumber(incrementEventNumber())
				.eventType(PollGameResponse.EventType.GameEnd)
				.selfScore(score)
				.leftScore(leftScore)
				.acrossScore(acrossScore)
				.rightScore(rightScore)
				.build()
		);
	}

	private synchronized int incrementEventNumber() {
		return ++currentEventNumber;
	}
}
