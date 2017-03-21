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
					nToChoose = random.nextInt() % candidates.size();
				}
				break;
			case FirstRound:
			case Playing:
				if (clientGameView.getTable().size() == 0) {
					// Leading.
					if (clientGameView.getGamePhase() == GameState.Phase.FirstRound) {
						candidates.add(new Card(Card.Rank.TWO, Card.Suit.CLUB));
					} else {
						List<Card> charged = new ArrayList<Card>();
						List<Card> hearts = new ArrayList<Card>();
						List<Card> chargedHeart = new ArrayList<Card>();

						for (Card c : clientGameView.getHand()) {
							if (clientGameView.getChargedCards().contains(c)) {
								if (clientGameView.getPlayedSuits().contains(c.getSuit())) {
									candidates.add(c);
								} else {
									if (c.getSuit() == Card.Suit.HEART && !clientGameView.isHeartsBroken()) {
										chargedHeart.add(c);
									} else {
										charged.add(c);
									}
								}
							} else if (c.getSuit() == Card.Suit.HEART && clientGameView.isHeartsBroken() == false) {
								hearts.add(c);
							} else {
								candidates.add(c);
							}
						}
						if (candidates.size() == 0) {
							candidates = charged;
						}
						if (candidates.size() == 0) {
							candidates = charged;
						}
						if (candidates.size() == 0) {
							candidates = charged;
						}
					}
				} else {
					// Following
					List<Card> suitedCharged = new ArrayList<Card>();
					List<Card> offSuit = new ArrayList<Card>();

					Card firstCard = clientGameView.getTable().get(0);
					for (Card c : clientGameView.getHand()) {
						if (c.getSuit() != firstCard.getSuit()) {
							// On the first round, hearts and QoS cannot be played.
							if (clientGameView.getGamePhase() != GameState.Phase.FirstRound
									|| (c.getSuit() != Card.Suit.HEART
										&& !c.equals(Card.QUEEN_OF_SPADES))) {
								offSuit.add(c);
							}
						} else if (clientGameView.getChargedCards().contains(c)) {
							suitedCharged.add(c);
						} else {
							// Suited, uncharged.
							candidates.add(c);
						}
					}
					if (candidates.size() == 0) { candidates = suitedCharged; }
					if (candidates.size() == 0) { candidates = offSuit; }
					// Rare, first round where player only has hearts and queen of spades.
					if (candidates.size() == 0) { candidates = clientGameView.getHand(); }
				}

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
	public void reportPass(GameState.PlayerPosition position, ArrayList<Card> card) {
		//pass
	}

	@Override
	public void reportCharge(GameState.PlayerPosition position, Card card) {
		//pass
	}
}
