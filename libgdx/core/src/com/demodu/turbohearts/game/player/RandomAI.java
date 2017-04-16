package com.demodu.turbohearts.game.player;

import com.demodu.turbohearts.gamelogic.Card;
import com.demodu.turbohearts.gamelogic.ClientGameView;
import com.demodu.turbohearts.gamelogic.GameConductor;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.gamelogic.PlayerActor;

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
		try {
			reporter.reportMove(candidates.subList(0, nToChoose));
		} catch (MoveReporter.InvalidMoveException ex) {
			throw new IllegalStateException("Error reporting move", ex);
		}
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
