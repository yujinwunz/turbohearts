package com.demodu.gamelogic;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.demodu.gamelogic.GameConductor.Phase.Charging;
import static com.demodu.gamelogic.GameConductor.Phase.Finished;
import static com.demodu.gamelogic.GameConductor.Phase.FirstRound;
import static com.demodu.gamelogic.GameConductor.Phase.Passing;
import static com.demodu.gamelogic.GameConductor.Phase.Playing;
import static com.demodu.gamelogic.GameConductor.Phase.Ready;
import static com.demodu.gamelogic.GameConductor.Round.Left;

public class LocalGameConductor extends GameConductor {
	private Player[] players;
	private int currentPlayer = 0;
	private GameConductor.Phase phase = Ready;
	private List<Card> table = new ArrayList<Card>();
	private GameConductor.Round round = Left;

	private boolean heartsBroken;
	private List<Card> chargedCards = new ArrayList<Card>();
	private List<Card.Suit> playedSuits = new ArrayList<Card.Suit>();

	private int numRounds = 0;

	public LocalGameConductor(PlayerActor left, PlayerActor across, PlayerActor right) {
		this.players = new Player[]{
				new Player(null),
				new Player(left),
				new Player(across),
				new Player(right),
		};
	}

	@Override
	public void registerPlayer(PlayerActor actor) {
		players[0].actor = actor;
		start();
	}

	private void start() {
		heartsBroken = false;
		playedSuits.clear();
		chargedCards.clear();

		ArrayList<Card> deck = new ArrayList<Card>(Card.getDeck());
		Collections.shuffle(deck);

		for (int i = 0; i < 4; i++) {
			players[i].hand.addAll(deck.subList(13*i, 13*i+13));
			players[i].actor.reportPass(PlayerPosition.Self, new ArrayList<Card>(players[i].hand));
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
				phase == Playing || phase == FirstRound ? getCurrentPlayerLegalMoves() : Collections.<Card>emptyList(),
				chargedCards,
				playedSuits
		);
	}

	private void startPassing() {
		clearPlayerStageActions();
		this.phase = Passing;
		if (this.round == GameConductor.Round.NoPass) {
			startCharging();
		} else {
			for (int i = 0; i < 4; i++) {
				final int fi = i;
				players[i].actor.getMove(
						makeClientView(i),
						new MoveReporter() {
							@Override
							protected void reportMoveImpl(List<Card> move) {
								// Validate
								if (move.size() != 3) {
									throw new IllegalArgumentException("Needs to pass 3 cards");
								}
								if (phase != Passing) {
									throw new IllegalStateException("Not in passing mode anymore");
								}
								for (Card c : move) {
									if (!players[fi].hand.contains(c)) {
										throw new IllegalStateException("Card " + c + " not in hand");
									}
								}
								players[fi].actionStage = new ArrayList<Card>(move);
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
						new ArrayList<Card>(players[i].actionStage)
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
		Gdx.app.log("GameConductor", "Starting to charge");
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
					protected void reportMoveImpl(List<Card> move) {
						for (Card c : move) {
							if (!players[fi].hand.contains(c)) {
								throw new IllegalArgumentException("Card " + c + " is not in hand");
							}
							if (!Card.getChargeableCards().contains(c)) {
								throw new IllegalArgumentException("Card " + c + " is not chargeable");
							}
						}
						for (int i = move.size() - 1; i >= 0; i--) {
							if (chargedCards.contains(move.get(i))) {
								move.remove(i);
							}
						}
						players[fi].actionStage = new ArrayList<Card>(move);
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
				Gdx.app.log("GameConductor", "still waiting on player " + i);
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
				for (Card c : players[i].actionStage) {
					for (PlayerPosition pos : PlayerPosition.values()) {
						players[(pos.getIndex() - i + 4) % 4].actor.reportCharge(pos, c);
					}
				}
			}

			if (hasCharged && chargedCards.size() < Card.getChargeableCards().size()) {
				startCharging(shouldChargeAgain);
			} else {
				startPlaying();
			}
		}
	}

	private void startPlaying() {

		phase = FirstRound;
		for (int i = 0; i < 4; i++) {
			if (players[i].hand.contains(Card.TWO_OF_CLUBS)) {
				currentPlayer = i;
				break;
			}
		}

		stepPlaying();
	}

	private void stepPlaying() {
		Gdx.app.log("GameConductor", "Playing. Current playing: " + currentPlayer);
		if (players[currentPlayer].hand.size() == 0) {
			// Game has ended.
			endTrick();
			endRound();
			Gdx.app.log("GameConductor", "Round has ended");
			return;
		} else {
			if (table.size() == 8 ||
					table.size() == 4 && (!table.contains(new Card(Card.Rank.NINE, table.get(0).getSuit())))) {
				// Trick ended
				endTrick();
			}
		}

		players[currentPlayer].actor.getMove(makeClientView(currentPlayer), new MoveReporter() {
			@Override
			protected void reportMoveImpl(List<Card> move) {
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

				if (move.get(0).getSuit() == Card.Suit.HEART) {
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
		Gdx.app.log("GameConductor", "Trick has ended");
		Card.Suit leadingSuit = table.get(0).getSuit();
		if (!playedSuits.contains(leadingSuit)) {
			playedSuits.add(leadingSuit);
		}
		// Because there's always 4 or 8 cards per trick, we know that
		// the currentPlayer is also the leader of the trick.
		int winningPlayer = currentPlayer;
		Card.Rank winningRank = table.get(0).getRank();
		for (Card c : table) {
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
			for (Card c : p.taken) {
				if (c.equals(Card.TEN_OF_CLUBS)) {
					hasTenOfClubs = true;
				}
				if (c.equals(Card.QUEEN_OF_SPADES)) {
					points += chargedCards.contains(Card.QUEEN_OF_SPADES) ? 26 : 13;
					nPointCards += 1;
				}
				if (c.getSuit() == Card.Suit.HEART) {
					points += chargedCards.contains(Card.ACE_OF_HEARTS) ? 2 : 1;
					nPointCards += 1;
				}
			}
			if (nPointCards == 14) {
				points *= -1;
			}
			if (p.taken.contains(Card.JACK_OF_DIAMONDS)) {
				points -= chargedCards.contains(Card.JACK_OF_DIAMONDS) ? 20 : 10;
			}
			if (hasTenOfClubs) {
				points *= chargedCards.contains(Card.TEN_OF_CLUBS) ? 4 : 2;
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
		round = GameConductor.Round.values()[(round.ordinal() + 1)%4];

		if (numRounds == 1) {
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
	private List<Card> getCurrentPlayerLegalMoves() {
		List<Card> candidates = new ArrayList<Card>();
		if (table.size() == 0) {
			// Leading.
			if (phase == GameConductor.Phase.FirstRound) {
				return Collections.singletonList(Card.TWO_OF_CLUBS);
			} else {
				List<Card> charged = new ArrayList<Card>();
				List<Card> hearts = new ArrayList<Card>();

				for (Card c : players[currentPlayer].hand) {
					if (chargedCards.contains(c)) {
						if (playedSuits.contains(c.getSuit())) {
							candidates.add(c);
						} else {
							if (c.getSuit() != Card.Suit.HEART || heartsBroken) {
								charged.add(c);
							}
						}
					} else if (c.getSuit() == Card.Suit.HEART && !heartsBroken) {
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
			List<Card> suitedCharged = new ArrayList<Card>();
			List<Card> offSuit = new ArrayList<Card>();

			Card firstCard = table.get(0);
			for (Card c : players[currentPlayer].hand) {
				if (c.getSuit() != firstCard.getSuit()) {
					// On the first round, hearts and QoS cannot be played.
					if (phase != GameConductor.Phase.FirstRound
							|| (c.getSuit() != Card.Suit.HEART
							&& !c.equals(Card.QUEEN_OF_SPADES))) {
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
			return new ArrayList<Card>(players[currentPlayer].hand);
		}
	}

	private void clearPlayerStageActions() {
		for (Player p : players) {
			p.actionStage = null;
		}
	}

	private static class Player {
		List<Card> hand;
		List<Card> taken;
		PlayerActor actor;
		List<Integer> points;
		// What the current player is trying to play but needs to wait for everyone to finish
		// (charging, passing)
		List<Card> actionStage;

		Player(PlayerActor actor) {
			this.actor = actor;
			reset();
		}

		void reset() {
			this.taken = new ArrayList<Card>();
			this.points = new ArrayList<Integer>();
			this.actionStage = null;
			this.hand = new ArrayList<Card>();
		}

		void appendPoints(int inc) {
			points.add(inc);
		}
	}

	
}
