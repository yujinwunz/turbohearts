package com.demodu.player;

import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.GameConductor;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gamelogic.PlayerActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomAI implements PlayerActor {
	private Random random = new Random();

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
					if (Card.getChargeableCards().contains(c)) {
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
	public void reportPlay(GameConductor.PlayerPosition position, Card card) {
		//pass
	}

	@Override
	public void reportPass(GameConductor.PlayerPosition position, List<Card> cards) {
		//pass
	}

	@Override
	public void reportCharge(GameConductor.PlayerPosition position, Card card) {
		//pass
	}

	@Override
	public void reportTrickEnd(GameConductor.PlayerPosition position) {

	}

	@Override
	public void reportRoundEnd(int score, int leftScore, int acrossScore, int rightScore) {

	}

	@Override
	public void reportGameEnd(int score, int leftScore, int acrossScore, int rightScore) {

	}
}
