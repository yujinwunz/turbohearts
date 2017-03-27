package com.demodu.turboheartsgame;

import com.badlogic.gdx.math.Vector2;
import com.demodu.GameContext;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.GameConductor;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gwtcompat.Callable;
import com.demodu.player.DelayedPlayer;

import java.util.ArrayList;
import java.util.List;

import static com.demodu.gamelogic.GameConductor.PlayerPosition.Self;

class UIPlayer extends DelayedPlayer {

	private TurboHeartsGame game;
	private GameContext gameContext;
	private Callable onGameEnd;

	UIPlayer(GameContext gameContext, TurboHeartsGame game, Callable onGameEnd) {
		super(0.25f, 1.0f);
		this.onGameEnd = onGameEnd;
		this.game = game;
		this.gameContext = gameContext;
	}

	@Override
	public void getMoveImpl(ClientGameView clientGameView, MoveReporter reporter) {
		if (game.clearTableOnNextPlay) {
			game.onTable.clear();
			game.clearTableOnNextPlay = false;
		}
		game.moveReporter = reporter;

		for (GdxCard c : game.getCards()) {
			c.setSelected(false);
		}

		switch (clientGameView.getGamePhase()) {
			case Passing:
				game.startPassing(clientGameView.getGameRound());
				break;
			case Charging:
				game.startCharging();
				break;
			case FirstRound:
			case Playing:
				game.startPlaying(clientGameView.getLegalPlays());
				break;
			default:
				game.enterWaiting();
				break;
		}
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public void reportPlayImpl(GameConductor.PlayerPosition position, Card card) {
		if (game.clearTableOnNextPlay) {
			game.onTable.clear();
			game.clearTableOnNextPlay = false;
		}
		double offsetX = 0, offsetY = 0, a = 0;
		switch (position) {
			case Left:
				offsetX = -50; a = Math.PI/2;
				break;
			case Across:
				offsetY = 50;
				break;
			case Right:
				offsetX = 50; a = Math.PI*3/2;
				break;
			case Self:
				offsetY = -50;
			default:
				break;
		}
		if (game.onTable.size() >= 4) {
			offsetX *= 1.7;
			offsetY *= 1.7;
		}
		Vector2 from = game.getCoordinatesOfOpponent(position);

		GdxCard c;
		if (position == Self) {
			c = game.getPlayerHand().removeCard(card);
		} else if (game.otherChargedCards.contains(card)) {
			c = game.otherChargedCards.get(game.otherChargedCards.indexOf(card));
			game.otherChargedCards.remove(card);
		} else {
			c = new GdxCard(
					card.getRank(),
					card.getSuit(),
					from.x,
					from.y,
					game.getPlayerHand().getHeight(),
					0,
					gameContext,
					GdxCard.State.Inactive
			);
		}

		if (game.onTable.size() >= 4){
			game.onTable.add(0, c);
		} else {
			game.onTable.add(c);
		}
		c.sendTo(
				(float)(offsetX + game.getWidth() / 2),
				(float)(offsetY + game.getHeight() / 2),
				(float)a
		);
	}

	@Override
	public void reportPassImpl(GameConductor.PlayerPosition position, List<Card> cards) {
		Vector2 from = game.getCoordinatesOfOpponent(position);
		ArrayList<GdxCard> newCards = new ArrayList<GdxCard>();

		for (Card c: cards) {
			GdxCard gc = new GdxCard(
					c.getRank(),
					c.getSuit(),
					from.x,
					from.y,
					game.getPlayerHand().getHeight(),
					0,
					gameContext,
					GdxCard.State.Inactive
			);
			newCards.add(gc);
			if (position != Self) {
				gc.setSelected(true);
			}
		}

		game.addCards(newCards);
	}

	@Override
	public void reportChargeImpl(GameConductor.PlayerPosition position, Card card) {

		if (position != Self) {
			float x = 0, y = 0, a = 0;
			switch (position) {
				case Left:
					y = game.getHeight()/2 + 50 + 30 * game.otherChargedCards.size(); a = (float)Math.PI/2;
					break;
				case Across:
					x = game.getWidth()/2 - 30 * game.otherChargedCards.size(); y = game.getHeight();
					break;
				case Right:
					y = game.getHeight()/2 + 50 + 30 * game.otherChargedCards.size(); x = game.getWidth(); a = (float)Math.PI/2;
					break;
			}


			game.otherChargedCards.add(new GdxCard(
					card.getRank(),
						card.getSuit(),
						x, y,
						game.getPlayerHand().getHeight(),
						a,
						gameContext,
						GdxCard.State.Inactive
				)
			);
			game.otherChargedCards.get(game.otherChargedCards.size()-1).setCharged(true);
		}
	}

	@Override
	public void reportTrickEndImpl(GameConductor.PlayerPosition position) {
		Vector2 flyTo = game.getCoordinatesOfOpponent(position);
		for (GdxCard c : game.onTable) {
			c.sendTo(flyTo.x, flyTo.y, (float)c.a);
		}
		// We'll remove the cards after they finish animating, ie on the next play.
		game.clearTableOnNextPlay = true;
	}

	@Override
	public void reportRoundEndImpl(int score, int leftScore, int acrossScore, int rightScore) {
		game.onTable.clear();
		game.otherChargedCards.clear();
		this.pause();
		game.roundEnd(score, leftScore, acrossScore, rightScore);
	}

	@Override
	public void reportGameEndImpl(int score, int leftScore, int acrossScore, int rightScore) {
		onGameEnd.call();
	}
}
