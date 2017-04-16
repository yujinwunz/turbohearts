package com.demodu.turbohearts.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.demodu.turbohearts.gamelogic.GameConductor.Phase.Charging;
import static com.demodu.turbohearts.gamelogic.GameConductor.Phase.Finished;
import static com.demodu.turbohearts.gamelogic.GameConductor.Phase.FirstRound;
import static com.demodu.turbohearts.gamelogic.GameConductor.Phase.Passing;
import static com.demodu.turbohearts.gamelogic.GameConductor.Phase.Playing;
import static com.demodu.turbohearts.gamelogic.GameConductor.Phase.Ready;
import static com.demodu.turbohearts.gamelogic.GameConductor.Round.Left;

public class LocalGameConductor extends com.demodu.turbohearts.gamelogic.GameConductor {
	public static final int NUM_ROUNDS_PER_GAME = 8;

	private Player[] players;
	private int currentPlayer = 0;
	private Phase phase = Ready;
	private List<com.demodu.turbohearts.gamelogic.Card> table = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
	private Round round = Left;

	private boolean heartsBroken;
	private List<com.demodu.turbohearts.gamelogic.Card> chargedCards = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
	private List<com.demodu.turbohearts.gamelogic.Card.Suit> playedSuits = new ArrayList<com.demodu.turbohearts.gamelogic.Card.Suit>();

	private int numRounds = 0;

	public LocalGameConductor(com.demodu.turbohearts.gamelogic.PlayerActor left, com.demodu.turbohearts.gamelogic.PlayerActor across, com.demodu.turbohearts.gamelogic.PlayerActor right) {
		this.players = new Player[]{
				new Player(null),
				new Player(left),
				new Player(across),
				new Player(right),
		};
	}

	@Override
	public void onRegisterPlayer(com.demodu.turbohearts.gamelogic.PlayerActor actor) {
		players[0].actor = actor;
		start();
	}

	private void start() {
		heartsBroken = false;
		playedSuits.clear();
		chargedCards.clear();

		ArrayList<com.demodu.turbohearts.gamelogic.Card> deck = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(com.demodu.turbohearts.gamelogic.Card.getDeck());
		Collections.shuffle(deck);

		for (int i = 0; i < 4; i++) {
			players[i].hand.addAll(deck.subList(13*i, 13*i+13));
			players[i].actor.reportPass(PlayerPosition.Self, new ArrayList<com.demodu.turbohearts.gamelogic.Card>(players[i].hand));
		}

		startPassing();
	}

	private ClientGameView makeClientView(int playerIndex) {
		return new ClientGameView(
				this.table,
				players[playerIndex].hand,
				phase,
				round,
				heartsBroken,
				phase == Playing || phase == FirstRound ? getCurrentPlayerLegalMoves() : Collections.<com.demodu.turbohearts.gamelogic.Card>emptyList(),
				chargedCards,
				playedSuits
		);
	}

	private void startPassing() {
		clearPlayerStageActions();
		this.phase = Passing;
		if (this.round == com.demodu.turbohearts.gamelogic.GameConductor.Round.NoPass) {
			startCharging();
		} else {
			for (int i = 0; i < 4; i++) {
				final int fi = i;
				players[i].actor.getMove(
						makeClientView(i),
						new MoveReporter() {
							@Override
							protected void reportMoveImpl(List<com.demodu.turbohearts.gamelogic.Card> move) {
								// Validate
								if (move.size() != 3) {
									throw new IllegalArgumentException("Needs to pass 3 cards");
								}
								if (phase != Passing) {
									throw new IllegalStateException("Not in passing mode anymore");
								}
								for (com.demodu.turbohearts.gamelogic.Card c : move) {
									if (!players[fi].hand.contains(c)) {
										throw new IllegalStateException("Card " + c + " not in hand");
									}
								}
								players[fi].actionStage = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(move);
								// Report the pass when all players have finished passing.

								stepPassing();
							}
						}
				);
			}
		}
	}

	private void stepPassing() {
		boolean playersReady = true;
		for (Player p: players) {
			if (p.actionStage == null) {
				playersReady = false;
			}
		}
		if (playersReady) {
			// Do the pass
			for (int i = 0; i < 4; i++) {
				int passToI = (i + round.getPassIndex())%4;

				players[passToI].hand.addAll(players[i].actionStage);
				players[i].hand.removeAll(players[i].actionStage);

				// Report the pass
				players[passToI].actor.reportPass(
						PlayerPosition.opposite(round),
						new ArrayList<com.demodu.turbohearts.gamelogic.Card>(players[i].actionStage)
				);

			}
			phase = Charging;
			startCharging();
		}
	}

	private void startCharging() {
		startCharging(true, true, true, true);
	}

	// Charging works like this:
	// Players each independently decide to charge their cards.
	// If no-one charges any new cards, the charging phase is over.
	// If a card is charged, players have the opportunity to react
	// and charge in a follow up round.
	// Up to 4 charging rounds (one for each chargable card) might be played.
	private void startCharging(boolean... shouldChargeAgain) {
		clearPlayerStageActions();
		this.phase = Charging;

		for (int i = 0; i < 4; i++) {
			if (!shouldChargeAgain[i]) {
				players[i].actionStage = Collections.emptyList();
			}
		}

		for (int i = 0; i < 4; i++) {
			if (shouldChargeAgain[i]) {
				final int fi = i;
				players[i].actionStage = null;
				players[i].actor.getMove(makeClientView(i), new MoveReporter() {
					@Override
					protected void reportMoveImpl(List<com.demodu.turbohearts.gamelogic.Card> move) {
						for (com.demodu.turbohearts.gamelogic.Card c : move) {
							if (!players[fi].hand.contains(c)) {
								throw new IllegalArgumentException("Card " + c + " is not in hand");
							}
							if (!com.demodu.turbohearts.gamelogic.Card.getChargeableCards().contains(c)) {
								throw new IllegalArgumentException("Card " + c + " is not chargeable");
							}
						}
						for (int i = move.size() - 1; i >= 0; i--) {
							if (chargedCards.contains(move.get(i))) {
								move.remove(i);
							}
						}
						players[fi].actionStage = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(move);
						// Reporting of charged cards only happen at the end of the
						// charging round.
						stepCharging();
					}
				});
			}
		}
	}

	private void stepCharging() {
		boolean playersReady = true;
		boolean shouldChargeAgain[] = new boolean[]{false, false, false, false};
		boolean hasCharged = false;
		for (int i = 0; i < 4; i++) {
			if (players[i].actionStage == null) {
				playersReady = false;
			} else if (players[i].actionStage.size() >= 1) {
				for (int j = 0; j < 4; j++) {
					if (j != i) {
						shouldChargeAgain[j] = true;
					}
				}
				hasCharged = true;
			}
		}
		if (playersReady) {
			for (int i = 0; i < 4; i++) {
				chargedCards.addAll(players[i].actionStage);
				// Report action
				for (com.demodu.turbohearts.gamelogic.Card c : players[i].actionStage) {
					for (PlayerPosition pos : PlayerPosition.values()) {
						players[(pos.getIndex() - i + 4) % 4].actor.reportCharge(pos, c);
					}
				}
			}

			if (hasCharged && chargedCards.size() < com.demodu.turbohearts.gamelogic.Card.getChargeableCards().size()) {
				startCharging(shouldChargeAgain);
			} else {
				startPlaying();
			}
		}
	}

	private void startPlaying() {

		phase = FirstRound;
		for (int i = 0; i < 4; i++) {
			if (players[i].hand.contains(com.demodu.turbohearts.gamelogic.Card.TWO_OF_CLUBS)) {
				currentPlayer = i;
				break;
			}
		}

		stepPlaying();
	}

	private void stepPlaying() {
		if (players[currentPlayer].hand.size() == 0) {
			// Game has ended.
			endTrick();
			endRound();
			return;
		} else {
			if (table.size() == 8 ||
					table.size() == 4 && (!table.contains(new com.demodu.turbohearts.gamelogic.Card(com.demodu.turbohearts.gamelogic.Card.Rank.NINE, table.get(0).getSuit())))) {
				// Trick ended
				endTrick();
			}
		}

		players[currentPlayer].actor.getMove(makeClientView(currentPlayer), new MoveReporter() {
			@Override
			protected void reportMoveImpl(List<com.demodu.turbohearts.gamelogic.Card> move) {
				if (move.size() != 1) {
					throw new IllegalArgumentException("Can't play more than one card");
				}
				if (!players[currentPlayer].hand.contains(move.get(0))) {
					throw new IllegalArgumentException("Card " + move.get(0) + " not in hand");
				}
				if (!getCurrentPlayerLegalMoves().contains(move.get(0))) {
					throw new IllegalArgumentException("Card " + move.get(0) + " is not a legal play");
				}

				// Report play.
				for (PlayerPosition pos : PlayerPosition.values()) {
					players[(currentPlayer - pos.getIndex()+4)%4].actor.reportPlay(
							pos, move.get(0)
					);
				}

				if (move.get(0).getSuit() == com.demodu.turbohearts.gamelogic.Card.Suit.HEART) {
					heartsBroken = true;
				}
				players[currentPlayer].hand.remove(move.get(0));
				table.addAll(move);
				currentPlayer = (currentPlayer + 1)%4;
				stepPlaying();
			}
		});
	}

	private void endTrick() {
		com.demodu.turbohearts.gamelogic.Card.Suit leadingSuit = table.get(0).getSuit();
		if (!playedSuits.contains(leadingSuit)) {
			playedSuits.add(leadingSuit);
		}
		// Because there's always 4 or 8 cards per trick, we know that
		// the currentPlayer is also the leader of the trick.
		int winningPlayer = currentPlayer;
		com.demodu.turbohearts.gamelogic.Card.Rank winningRank = table.get(0).getRank();
		for (com.demodu.turbohearts.gamelogic.Card c : table) {
			if (c.getRank().ordinal() > winningRank.ordinal() && c.getSuit() == leadingSuit) {
				winningRank = c.getRank();
				winningPlayer = currentPlayer;
			}
			currentPlayer = (currentPlayer + 1) % 4;
		}

		// Not first trick anymore
		phase = Playing;
		players[winningPlayer].taken.addAll(table);
		currentPlayer = winningPlayer;
		table.clear();

		for (int i = 0; i < 4; i++) {
			players[i].actor.reportTrickEnd(PlayerPosition.values()[(winningPlayer - i + 4)%4]);
		}
	}

	private void endRound() {
		numRounds ++;
		for (Player p : players) {
			int points = 0;
			boolean hasTenOfClubs = false;
			int nPointCards = 0;
			for (com.demodu.turbohearts.gamelogic.Card c : p.taken) {
				if (c.equals(com.demodu.turbohearts.gamelogic.Card.TEN_OF_CLUBS)) {
					hasTenOfClubs = true;
				}
				if (c.equals(com.demodu.turbohearts.gamelogic.Card.QUEEN_OF_SPADES)) {
					points += chargedCards.contains(com.demodu.turbohearts.gamelogic.Card.QUEEN_OF_SPADES) ? 26 : 13;
					nPointCards += 1;
				}
				if (c.getSuit() == com.demodu.turbohearts.gamelogic.Card.Suit.HEART) {
					points += chargedCards.contains(com.demodu.turbohearts.gamelogic.Card.ACE_OF_HEARTS) ? 2 : 1;
					nPointCards += 1;
				}
			}
			if (nPointCards == 14) {
				points *= -1;
			}
			if (p.taken.contains(com.demodu.turbohearts.gamelogic.Card.JACK_OF_DIAMONDS)) {
				points -= chargedCards.contains(com.demodu.turbohearts.gamelogic.Card.JACK_OF_DIAMONDS) ? 20 : 10;
			}
			if (hasTenOfClubs) {
				points *= chargedCards.contains(com.demodu.turbohearts.gamelogic.Card.TEN_OF_CLUBS) ? 4 : 2;
			}

			p.appendPoints(points);
		}

		for (int i = 0; i < 4; i++) {
			players[i].actor.reportRoundEnd(
					players[i].points.get(players[i].points.size() - 1),
					players[(i + 1) % 4].points.get(players[(i + 1) % 4].points.size() - 1),
					players[(i + 2) % 4].points.get(players[(i + 2) % 4].points.size() - 1),
					players[(i + 3) % 4].points.get(players[(i + 3) % 4].points.size() - 1)
			);
		}
		for (Player p: players) {
			p.reset();
		}
		round = com.demodu.turbohearts.gamelogic.GameConductor.Round.values()[(round.ordinal() + 1)%4];

		if (numRounds == NUM_ROUNDS_PER_GAME) {
			phase = Finished;
			for (int i = 0; i < 4; i++) {
				players[i].actor.reportGameEnd(
						sumScores(players[i].points),
						sumScores(players[(i + 1) % 4].points),
						sumScores(players[(i + 2) % 4].points),
						sumScores(players[(i + 3) % 4].points)
				);
			}
		} else {
			start();
		}
	}

	private int sumScores(List<Integer> scores) {
		int sum = 0;
		for (Integer i : scores) {
			sum += i;
		}
		return sum;
	}

	/**
	 * In playing mode, returns legal moves for leading and following plays.
	 * Not valid for passing or charging turns.
	 */
	private List<com.demodu.turbohearts.gamelogic.Card> getCurrentPlayerLegalMoves() {
		List<com.demodu.turbohearts.gamelogic.Card> candidates = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
		if (table.size() == 0) {
			// Leading.
			if (phase == com.demodu.turbohearts.gamelogic.GameConductor.Phase.FirstRound) {
				return Collections.singletonList(com.demodu.turbohearts.gamelogic.Card.TWO_OF_CLUBS);
			} else {
				List<com.demodu.turbohearts.gamelogic.Card> charged = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
				List<com.demodu.turbohearts.gamelogic.Card> hearts = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();

				for (com.demodu.turbohearts.gamelogic.Card c : players[currentPlayer].hand) {
					if (chargedCards.contains(c)) {
						if (playedSuits.contains(c.getSuit())) {
							candidates.add(c);
						} else {
							if (c.getSuit() != com.demodu.turbohearts.gamelogic.Card.Suit.HEART || heartsBroken) {
								charged.add(c);
							}
						}
					} else if (c.getSuit() == com.demodu.turbohearts.gamelogic.Card.Suit.HEART && !heartsBroken) {
						hearts.add(c);
					} else {
						candidates.add(c);
					}
				}
				if (candidates.size() != 0) return candidates;
				if (charged.size() != 0) return charged;
				if (hearts.size() != 0) return hearts;
				throw new IllegalStateException("No card can be lead");
			}
		} else {
			// Following
			List<com.demodu.turbohearts.gamelogic.Card> suitedCharged = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
			List<com.demodu.turbohearts.gamelogic.Card> offSuit = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();

			com.demodu.turbohearts.gamelogic.Card firstCard = table.get(0);
			for (com.demodu.turbohearts.gamelogic.Card c : players[currentPlayer].hand) {
				if (c.getSuit() != firstCard.getSuit()) {
					// On the first round, hearts and QoS cannot be played.
					if (phase != com.demodu.turbohearts.gamelogic.GameConductor.Phase.FirstRound
							|| (c.getSuit() != com.demodu.turbohearts.gamelogic.Card.Suit.HEART
							&& !c.equals(com.demodu.turbohearts.gamelogic.Card.QUEEN_OF_SPADES))) {
						offSuit.add(c);
					}
				} else if (chargedCards.contains(c) && !playedSuits.contains(c.getSuit())) {
					suitedCharged.add(c);
				} else {
					// Suited, uncharged.
					candidates.add(c);
				}
			}
			if (candidates.size() != 0) return candidates;
			if (suitedCharged.size() != 0) return suitedCharged;
			if (offSuit.size() != 0) return offSuit;
			// Rare, first round where player only has hearts and/or queen of spades.
			// I cannot find documentation whether the official rules call for
			// (any card can be played) or (only QoS/uncharged heart can be played).
			return new ArrayList<com.demodu.turbohearts.gamelogic.Card>(players[currentPlayer].hand);
		}
	}

	private void clearPlayerStageActions() {
		for (Player p : players) {
			p.actionStage = null;
		}
	}

	private static class Player {
		List<com.demodu.turbohearts.gamelogic.Card> hand;
		List<com.demodu.turbohearts.gamelogic.Card> taken;
		com.demodu.turbohearts.gamelogic.PlayerActor actor;
		List<Integer> points;
		// What the current player is trying to play but needs to wait for everyone to finish
		// (charging, passing)
		List<com.demodu.turbohearts.gamelogic.Card> actionStage;

		Player(com.demodu.turbohearts.gamelogic.PlayerActor actor) {
			this.actor = actor;
			reset();
		}

		void reset() {
			this.taken = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
			this.points = new ArrayList<Integer>();
			this.actionStage = null;
			this.hand = new ArrayList<com.demodu.turbohearts.gamelogic.Card>();
		}

		void appendPoints(int inc) {
			points.add(inc);
		}
	}

	
}
