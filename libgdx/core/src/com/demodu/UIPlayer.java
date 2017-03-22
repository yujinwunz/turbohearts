package com.demodu;

import com.badlogic.gdx.math.Vector2;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.GameState;
import com.demodu.gamelogic.MoveReporter;

import java.util.ArrayList;
import java.util.List;

import static com.demodu.gamelogic.GameState.PlayerPosition.Self;

/**
 * Created by yujinwunz on 22/03/2017.
 */

public class UIPlayer extends DelayedPlayer {

	private TurboHeartsGame game;

	public UIPlayer(TurboHeartsGame game) {
		super(0.25f, 1.0f);
		this.game = game;
	}

	@Override
	public void getMoveImpl(ClientGameView clientGameView, MoveReporter reporter) {
		if (game.clearTableOnNextPlay) {
			game.onTable.clear();
			game.clearTableOnNextPlay = false;
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
				game.phase = TurboHeartsGame.Phase.Waiting;
				break;
		}
		game.moveReporter = reporter;
	}

	@Override
	public void reportPlayImpl(GameState.PlayerPosition position, Card card) {
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
		if (game.otherChargedCards.contains(card)) {
			c = game.otherChargedCards.get(game.otherChargedCards.indexOf(card));
			game.otherChargedCards.remove(card);
		} else {
			c = new GdxCard(
					card.getRank(),
					card.getSuit(),
					from.x,
					from.y,
					game.playerHand.getHeight(),
					0,
					game.turboHearts,
					GdxCard.State.Inactive
			);
		}

		if (game.onTable.size() >= 4){
			game.onTable.add(0, c);
		} else {
			game.onTable.add(c);
		}
		c.sendTo(
				(float)(offsetX + game.width / 2),
				(float)(offsetY + game.height / 2),
				(float)a
		);
	}

	@Override
	public void reportPassImpl(GameState.PlayerPosition position, List<Card> cards) {
		Vector2 from = game.getCoordinatesOfOpponent(position);
		ArrayList<GdxCard> newCards = new ArrayList<GdxCard>();

		for (Card c: cards) {
			newCards.add(new GdxCard(
							c.getRank(),
							c.getSuit(),
							from.x,
							from.y,
							game.playerHand.getHeight(),
							0,
							game.turboHearts,
							GdxCard.State.Inactive
					)
			);
		}

		game.addCards(newCards);
	}

	@Override
	public void reportChargeImpl(GameState.PlayerPosition position, Card card) {
		if (position != Self) {
			float x = 0, y = 0, a = 0;
			switch (position) {
				case Self:
					break;
				case Left:
					y = game.height/2 - 30 * game.otherChargedCards.size(); a = (float)Math.PI/2;
					break;
				case Across:
					x = game.width/2 - 30 * game.otherChargedCards.size(); y = game.height;
					break;
				case Right:
					y = game.height/2 + 30 * game.otherChargedCards.size(); x = game.width; a = (float)Math.PI/2;
					break;
			}


			game.otherChargedCards.add(new GdxCard(
					card.getRank(),
						card.getSuit(),
						x, y,
						game.playerHand.getHeight(),
						a,
						game.turboHearts,
						GdxCard.State.Inactive
				)
			);
			game.otherChargedCards.get(game.otherChargedCards.size()-1).setCharged(true);
		}
	}

	@Override
	public void reportTrickEndImpl(GameState.PlayerPosition position) {
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
		game.roundEnd();
	}

	@Override
	public void reportGameEndImpl(int score, int leftScore, int acrossScore, int rightScore) {
		// Pass
	}
}
