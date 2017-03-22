package com.demodu;

import com.badlogic.gdx.Gdx;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.GameState;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gamelogic.PlayerActor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yujinwunz on 22/03/2017.
 */

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
			public Object call() throws Exception {
				getMoveImpl(clientGameView, reporter);
				return null;
			}
		}));
	}

	@Override
	public void reportPlay(final GameState.PlayerPosition position, final Card card) {
		if (position != GameState.PlayerPosition.Self){
			endOfQueue += cardPlayDelay;
		}
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() throws Exception {
				reportPlayImpl(position, card);
				return null;
			}
		}));
	}

	@Override
	public void reportPass(final GameState.PlayerPosition position, final List<Card> cards) {
		endOfQueue += cardPlayDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() throws Exception {
				reportPassImpl(position, cards);
				return null;
			}
		}));
	}

	@Override
	public void reportCharge(final GameState.PlayerPosition position, final Card card) {
		if (position != GameState.PlayerPosition.Self){
			endOfQueue += cardPlayDelay;
		}
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() throws Exception {
				reportChargeImpl(position, card);
				return null;
			}
		}));
	}

	@Override
	public void reportTrickEnd(final GameState.PlayerPosition position) {
		endOfQueue += roundEndDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() throws Exception {
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
			public Object call() throws Exception {
				reportRoundEndImpl(score, leftScore, acrossScore, rightScore);
				return null;
			}
		}));
	}

	@Override
	public void reportGameEnd(final int score, final int leftScore, final int acrossScore, final int rightScore) {
		endOfQueue += roundEndDelay;
		schedule.add(new ScheduleElement(endOfQueue, new Callable() {
			@Override
			public Object call() throws Exception {
				reportGameEndImpl(score, leftScore, rightScore, acrossScore);
				return null;
			}
		}));
	}

	public abstract void getMoveImpl(ClientGameView clientGameView, MoveReporter reporter);

	public abstract void reportPlayImpl(GameState.PlayerPosition position, Card card);

	public abstract void reportPassImpl(GameState.PlayerPosition position, List<Card> cards);

	public abstract void reportChargeImpl(GameState.PlayerPosition position, Card card);

	public abstract void reportTrickEndImpl(GameState.PlayerPosition position);

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
