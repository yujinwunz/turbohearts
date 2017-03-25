package com.demodu.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a full information game :) This is the view of the game given to Ai's.
 */
public class ClientGameView {
	private GameConductor.Phase gamePhase;

	private GameConductor.Round gameRound;

	private ArrayList<Card> table;
	private ArrayList<Card> hand;

	private ArrayList<Card> legalPlays;

	private boolean heartsBroken;

	private ArrayList<Card> chargedCards;
	private ArrayList<Card.Suit> playedSuits;


	public ClientGameView(List<Card> table, List<Card> hand, GameConductor.Phase gamePhase,
						  GameConductor.Round gameRound, boolean heartsBroken, List<Card> legalPlays,
						  List<Card> chargedCards, List<Card.Suit> playedSuits
	) {
		this.table = new ArrayList<Card>(table);
		this.hand = new ArrayList<Card>(hand);
		this.gamePhase = gamePhase;
		this.gameRound = gameRound;
		this.heartsBroken = heartsBroken;
		this.legalPlays = new ArrayList<Card>(legalPlays);
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

	public ArrayList<Card> getLegalPlays() {
		return legalPlays;
	}

	public ArrayList<Card> getChargedCards() {
		return chargedCards;
	}

	public ArrayList<Card.Suit> getPlayedSuits() {
		return playedSuits;
	}

	public GameConductor.Phase getGamePhase() {
		return gamePhase;
	}

	public GameConductor.Round getGameRound() {
		return gameRound;
	}
}
