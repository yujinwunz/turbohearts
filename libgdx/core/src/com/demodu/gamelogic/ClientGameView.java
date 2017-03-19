package com.demodu.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a full information game :) This is the view of the game given to Ai's.
 */
public class ClientGameView {
	Config gameConfig;
	GameState.Phase gamePhase;

	ArrayList<Card> table;
	ArrayList<Card> hand;
	ArrayList<Card> lastRound;

	boolean heartsBroken;

	ArrayList<Card> chargedCards;
	ArrayList<Card.Suit> playedSuits;


	public ClientGameView(ArrayList<Card> table, ArrayList<Card> hand, ArrayList<Card> lastRound,
						  boolean heartsBroken, ArrayList<Card> chargedCards, ArrayList<Card.Suit> playedSuits
	) {
		this.table = new ArrayList<Card>(table);
		this.hand = new ArrayList<Card>(hand);
		this.lastRound = new ArrayList<Card>(lastRound);
		this.heartsBroken = heartsBroken;
		this.chargedCards = chargedCards;
		this.playedSuits = playedSuits;
	}

	public List<Card> getTable() {
		return new ArrayList<Card>(table);
	}

	public List<Card> getHand() {
		return new ArrayList<Card>(hand);
	}

	public List<Card> getLastRound() {
		return new ArrayList<Card>(lastRound);
	}
	public boolean isHeartsBroken() {
		return heartsBroken;
	}

	public ArrayList<Card> getChargedCards() {
		return chargedCards;
	}

	public ArrayList<Card.Suit> getPlayedSuits() {
		return playedSuits;
	}

	public Config getGameConfig() {
		return gameConfig;
	}

	public GameState.Phase getGamePhase() {
		return gamePhase;
	}

}
