package com.demodu;

import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.GameState;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gamelogic.PlayerActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by yujinwunz on 19/03/2017.
 */

public class RandomAI implements PlayerActor {
	public static final HashSet<Card> chargeableCards = new HashSet<Card>();

	{
		chargeableCards.add(new Card(Card.Rank.ACE, Card.Suit.HEART));
		chargeableCards.add(new Card(Card.Rank.QUEEN, Card.Suit.SPADE));
		chargeableCards.add(new Card(Card.Rank.JACK, Card.Suit.DIAMOND));
		chargeableCards.add(new Card(Card.Rank.TEN, Card.Suit.CLUB));
	}

	public Random random = new Random();

	@Override
	public void getMove(ClientGameView clientGameView, MoveReporter reporter) {
		List<Card> candidates = new ArrayList<Card>();
		int nToChoose = 1;

		switch (clientGameView.getGamePhase()) {

			case Ready:
				break;
			case Passing:
				nToChoose = 3;
				candidates = clientGameView.getHand();
				break;
			case Charging:
				for (Card c : clientGameView.getHand()) {
					if (chargeableCards.contains(c)) {
						candidates.add(c);
					}
					nToChoose = Math.abs(random.nextInt()) % (candidates.size() + 1);
				}
				break;
			case FirstRound:
			case Playing:
				candidates = clientGameView.getLegalPlays();
				break;
			case Finished:
				break;
		}

		Collections.shuffle(candidates);
		reporter.reportMove(candidates.subList(0, nToChoose));
	}

	@Override
	public void reportPlay(GameState.PlayerPosition position, Card card) {
		//pass
	}

	@Override
	public void reportPass(GameState.PlayerPosition position, List<Card> cards) {
		//pass
	}

	@Override
	public void reportCharge(GameState.PlayerPosition position, Card card) {
		//pass
	}
}
