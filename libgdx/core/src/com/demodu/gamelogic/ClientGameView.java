package com.demodu.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a full information game :) This is the view of the game given to Ai's.
 */
public class ClientGameView {
	Config gameConfig;
	GameState.Phase gamePhase;

	GameState.Round gameRound;

	ArrayList<Card> table;
	ArrayList<Card> hand;

	boolean heartsBroken;

	ArrayList<Card> chargedCards;
	ArrayList<Card.Suit> playedSuits;


	public ClientGameView(ArrayList<Card> table, ArrayList<Card> hand, GameState.Phase gamePhase,
						  GameState.Round gameRound, boolean heartsBroken,
						  ArrayList<Card> chargedCards, ArrayList<Card.Suit> playedSuits
	) {
		this.table = new ArrayList<Card>(table);
		this.hand = new ArrayList<Card>(hand);
		this.gamePhase = gamePhase;
		this.gameRound = gameRound;
		this.heartsBroken = heartsBroken;
		this.chargedCards = new ArrayList<Card>(chargedCards);
		this.playedSuits = new ArrayList<Card.Suit>(playedSuits);
	}

	public List<Card> getTable() {
		return new ArrayList<Card>(table);
	}

	public List<Card> getHand() {
		return new ArrayList<Card>(hand);
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

	public GameState.Round getGameRound() {
		return gameRound;
	}
}
