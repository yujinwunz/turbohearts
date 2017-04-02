package com.demodu.player;

import com.badlogic.gdx.Gdx;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.GameConductor;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gamelogic.PlayerActor;
import com.demodu.gwtcompat.Callable;

import java.util.ArrayList;
import java.util.List;

public abstract class DelayedPlayer implements PlayerActor {

	private ArrayList<ScheduleElement> schedule;
	private float currentTime = 0;
	private float endOfQueue = 0;

	private float cardPlayDelay;
	private float roundEndDelay;
	private boolean paused = false;

	public DelayedPlayer(float cardPlayDelay, float roundEndDelay) {
		schedule = new ArrayList<ScheduleElement>();
		this.cardPlayDelay = cardPlayDelay;
		this.roundEndDelay = roundEndDelay;
	}

	@Override
	public void getMove(final ClientGameView clientGameView, final MoveReporter reporter) {
		endOfQueue += cardPlayDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				getMoveImpl(clientGameView, reporter);
				return null;
			}
		}));
	}

	@Override
	public void reportPlay(final GameConductor.PlayerPosition position, final Card card) {
		if (position != GameConductor.PlayerPosition.Self){
			endOfQueue += cardPlayDelay;
		}
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				reportPlayImpl(position, card);
				return null;
			}
		}));
	}

	@Override
	public void reportPass(final GameConductor.PlayerPosition position, final List<Card> cards) {
		endOfQueue += cardPlayDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				reportPassImpl(position, cards);
				return null;
			}
		}));

		// Add a delay when we receive cards from the opponent
		if (position != GameConductor.PlayerPosition.Self) {
			endOfQueue += roundEndDelay;
		}
	}

	@Override
	public void reportCharge(final GameConductor.PlayerPosition position, final Card card) {
		if (position != GameConductor.PlayerPosition.Self){
			endOfQueue += cardPlayDelay;
		}
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				reportChargeImpl(position, card);
				return null;
			}
		}));
	}

	@Override
	public void reportTrickEnd(final GameConductor.PlayerPosition position) {
		endOfQueue += roundEndDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				reportTrickEndImpl(position);
				return null;
			}
		}));
	}

	@Override
	public void reportRoundEnd(final int score, final int leftScore, final int acrossScore, final int rightScore) {
		endOfQueue += roundEndDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				reportRoundEndImpl(score, leftScore, acrossScore, rightScore);
				return null;
			}
		}));
	}

	@Override
	public void reportGameEnd(final int score, final int leftScore, final int acrossScore, final int rightScore) {
		endOfQueue += 0.0; // Immediately report end of game after end of round.
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() {
				reportGameEndImpl(score, leftScore, rightScore, acrossScore);
				return null;
			}
		}));
	}

	public abstract void getMoveImpl(ClientGameView clientGameView, MoveReporter reporter);

	public abstract void reportPlayImpl(GameConductor.PlayerPosition position, Card card);

	public abstract void reportPassImpl(GameConductor.PlayerPosition position, List<Card> cards);

	public abstract void reportChargeImpl(GameConductor.PlayerPosition position, Card card);

	public abstract void reportTrickEndImpl(GameConductor.PlayerPosition position);

	public abstract void reportRoundEndImpl(int score, int leftScore, int acrossScore, int rightScore);

	public abstract void reportGameEndImpl(int score, int leftScore, int acrossScore, int rightScore);

	public void incrementTime(float delta) {
		currentTime += delta;
		endOfQueue = Math.max(endOfQueue, currentTime);
		while (schedule.size() > 0 && schedule.get(0).getTriggerTime() < currentTime) {
			try {
				schedule.get(0).getCallable().call();
			} catch (Exception e) {
				Gdx.app.error("DelayedPlayer", "error calling callback", e);
			}
			schedule.remove(0);
		}
	}

	private static class ScheduleElement {
		private float triggerTime;
		private Callable callable;

		public ScheduleElement(float triggerTime, Callable callable) {
			this.triggerTime = triggerTime;
			this.callable = callable;
		}

		public float getTriggerTime() {
			return triggerTime;
		}

		public Callable getCallable() {
			return callable;
		}
	}

	protected void pause() {
		paused = true;
	}

	private void resume() {
		paused = false;
	}
}
