package com.demodu.gamelogic;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.demodu.gamelogic.GameState.Phase.Charging;
import static com.demodu.gamelogic.GameState.Phase.FirstRound;
import static com.demodu.gamelogic.GameState.Phase.Passing;
import static com.demodu.gamelogic.GameState.Phase.Playing;
import static com.demodu.gamelogic.GameState.Phase.Ready;

public class GameState {
	private Player[] players;
	private int currentPlayer;
	private Phase phase;
	private ArrayList<Card> table;
	private ArrayList<Card> lastTrick;
	private Config config;
	private Round round;

	private boolean heartsBroken;
	private ArrayList<Card> chargedCards;
	private ArrayList<Card.Suit> playedSuits;

	public GameState(Config config, PlayerActor[] actors) {
		assert(actors.length == 4);
		this.players = new Player[]{
			new Player(actors[0]),
			new Player(actors[1]),
			new Player(actors[2]),
			new Player(actors[3]),
		};
		this.currentPlayer = 0;
		this.phase = Ready;
		this.table = new ArrayList<Card>();
		this.lastTrick = new ArrayList<Card>();
		this.config = config;
		this.round = Round.Left;

		this.heartsBroken = false;
		this.chargedCards = new ArrayList<Card>();
		this.playedSuits = new ArrayList<Card.Suit>();
	}

	public void start() {
		ArrayList<Card> deck = new ArrayList<Card>(Card.getDeck());
		Collections.shuffle(deck);

		for (int i = 0; i < 4; i++) {
			players[i].setHand(deck.subList(13*i, 13*i+13));
			players[i].actor.reportPass(PlayerPosition.Self, players[i].hand);
		}

		startPassing();
	}

	// Progress the game by one move. This function is called after every player action.
	private void step() {
		switch (this.phase) {
			case Ready:
				this.phase = Passing;
				step();
				break;
			case Passing:
				stepPassing();
				break;
			case Charging:
				stepCharging();
				break;
			case FirstRound:
			case Playing:
				stepPlaying();
				break;
			case Finished:
				break;
		}
	}

	private ClientGameView makeClientView(int playerIndex) {
		return new ClientGameView(
				this.table,
				players[playerIndex].hand,
				phase,
				round,
				heartsBroken,
				chargedCards,
				playedSuits
		);
	}

	private void startPassing() {
		clearPlayerStageActions();
		this.phase = Passing;
		if (this.round == Round.NoPass) {
			this.phase = Phase.Charging;
			step();
		} else {
			for (int i = 0; i < 4; i++) {
				final int fi = i;
				players[i].actor.getMove(
						makeClientView(i),
						new MoveReporter() {
							@Override
							protected void reportMoveImpl(List<Card> move) {
								// Validate
								assert (move.size() == 3);
								assert (phase == Passing);
								for (Card c : move) {
									assert(players[fi].hand.contains(c));
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

				// Report the pass
				players[passToI].actor.reportPass(
						PlayerPosition.opposite(round),
						new ArrayList<Card>(players[i].getActionStage())
				);

				players[passToI].hand.addAll(players[i].getActionStage());
				players[i].hand.removeAll(players[i].getActionStage());
			}
			phase = Charging;
			startCharging();
		}
	}

	// Charging works like this:
	// Players each independently decide to charge their cards.
	// If no-one charges any new cards, the charging phase is over.
	// If a card is charged, players have the opportunity to react
	// and charge in a follow up round.
	// Up to 4 charging rounds (one for each chargable card) might be played.
	private void startCharging() {
		clearPlayerStageActions();
		this.phase = Charging;
		for (int i = 0; i < 4; i++) {
			final int fi = i;
			players[i].actionStage = null;
			players[i].actor.getMove(makeClientView(i), new MoveReporter() {
				@Override
				protected void reportMoveImpl(List<Card> move) {
					for (Card c : move) {
						assert(players[fi].hand.contains(c));
						assert(c == Card.ACE_OF_HEARTS ||
								c == Card.QUEEN_OF_SPADES ||
								c == Card.JACK_OF_DIAMONDS ||
								c == Card.TEN_OF_CLUBS);
						assert(!chargedCards.contains(c));
					}
					players[fi].actionStage = new ArrayList<Card>(move);
					// Reporting of charged cards only happen at the end of the
					// charging round.
					stepCharging();
				}
			});
		}
	}

	private void stepCharging() {
		boolean playersReady = true;
		boolean chargingFinished = true;
		for (Player p: players) {
			if (p.actionStage == null) {
				playersReady = false;
			} else if (p.actionStage.size() > 1) {
				chargingFinished = false;
			}
		}
		if (playersReady) {
			for (int i = 0; i < 4; i++) {
				chargedCards.addAll(players[i].getActionStage());
				// Report action
				for (Card c : players[i].getActionStage()) {
					for (PlayerPosition pos : PlayerPosition.values()) {
						if (pos != PlayerPosition.Self) {
							players[(i + pos.getIndex()) % 4].actor.reportCharge(pos, c);
						}
					}
				}
			}
			if (chargingFinished) {
				startPlaying();
			} else {
				startCharging(); // Go for another round of charging
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
		if (players[currentPlayer].hand.size() == 0) {
			// Game has ended.
			endTrick();
			endGame();
			return;
		} else {
			if (table.size() == 8 ||
					table.size() == 4 && (table.contains(new Card(Card.Rank.NINE, table.get(0).getSuit())))) {
				// Trick ended
				endTrick();
			}
		}

		players[currentPlayer].actor.getMove(makeClientView(currentPlayer), new MoveReporter() {
			@Override
			protected void reportMoveImpl(List<Card> move) {
				assert(move.size() == 1);
				assert(players[currentPlayer].hand.contains(move.get(0)));

				// TODO: verify it's a valid play.

				// Report play.
				for (PlayerPosition pos : PlayerPosition.values()) {
					if (pos != PlayerPosition.Self) {
						players[(currentPlayer + pos.getIndex())%4].actor.reportPlay(
								pos, move.get(0)
						);
					}
				}

				players[currentPlayer].hand.remove(move.get(0));
				table.addAll(move);
				currentPlayer = (currentPlayer + 1)%4;
				stepPlaying();
			}
		});
		currentPlayer = (currentPlayer + 1) % 4;

	}

	private void endTrick() {
		Card.Suit leadingSuit = table.get(0).getSuit();
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
		lastTrick.clear();
		lastTrick.addAll(table);
		players[winningPlayer].taken.addAll(table);
		currentPlayer = winningPlayer;
		table.clear();
	}

	private void endGame() {
		for (Player p : players) {
			int points = 0;
			boolean hasTenOfClubs = false;
			for (Card c : p.taken) {
				if (c == Card.TEN_OF_CLUBS) {
					hasTenOfClubs = true;
				}
				if (c == Card.QUEEN_OF_SPADES) {
					points += chargedCards.contains(Card.QUEEN_OF_SPADES) ? 26 : 13;
				}
				if (c.getSuit() == Card.Suit.HEART) {
					points += chargedCards.contains(Card.ACE_OF_HEARTS) ? 2 : 1;
				}
				if (c == Card.JACK_OF_DIAMONDS) {
					points -= chargedCards.contains(Card.JACK_OF_DIAMONDS) ? 20 : 10;
				}
			}
			if (hasTenOfClubs) {
				points *= 2;
			}

			p.incrementPointsTotal(points);
		}

		// Any more games happening?
		start();
	}

	private void clearPlayerStageActions() {
		for (Player p : players) {
			p.actionStage = null;
		}
	}

	private static class Player {
		ArrayList<Card> hand;
		ArrayList<Card> taken;
		PlayerActor actor;
		private int pointsTotal;
		// What the current player is trying to play but needs to wait for everyone to finish
		// (charging, passing)
		ArrayList<Card> actionStage;

		public Player(PlayerActor actor) {
			this.actor = actor;
			this.taken = new ArrayList<Card>();
			this.pointsTotal = 0;
			this.actionStage = null;
			this.hand = new ArrayList<Card>();
		}

		public List<Card> getHand() {
			return hand;
		}

		public void setHand(List<Card> cards) {
			this.hand.clear();
			this.hand.addAll(cards);
		}

		public ArrayList<Card> getActionStage() {
			return actionStage;
		}

		public void setActionStage(ArrayList<Card> actionStage) {
			this.actionStage = actionStage;
		}

		public PlayerActor getActor() {
			return actor;
		}

		public int getPointsTotal() {
			return pointsTotal;
		}

		public void incrementPointsTotal(int inc) {
			this.pointsTotal += inc;
		}

	}

	public enum Phase {
		Ready,
		Passing,
		Charging,
		FirstRound,
		Playing,
		Finished
	}

	public enum Round {

		Left(1), Across(2), Right(3), NoPass(0);

		private int index;
		Round(int index) {
			this.index = index;
		}

		public int getPassIndex() {
			return index;
		}
	}

	public enum PlayerPosition {
		Left(1), Across(2), Right(3), Self(0);

		private int index;

		PlayerPosition(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public static PlayerPosition opposite(PlayerPosition p) {
			switch (p) {
				case Left:
					return Right;
				case Across:
					return Self;
				case Right:
					return Left;
				case Self:
					return Across;
				default:
					throw new IllegalArgumentException();
			}
		}

		public static PlayerPosition opposite(Round r) {
			switch (r) {
				case Left:
					return Right;
				case Across:
					return Self;
				case Right:
					return Left;
				case NoPass:
					return Across;
				default:
					throw new IllegalArgumentException();
			}
		}
	}
}
