package com.demodu.turbohearts.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a full information game :) This is the view of the game given to Ai's.
 */
public class ClientGameView {
	private com.demodu.turbohearts.gamelogic.GameConductor.Phase gamePhase;

	private com.demodu.turbohearts.gamelogic.GameConductor.Round gameRound;

	private ArrayList<com.demodu.turbohearts.gamelogic.Card> table;
	private ArrayList<com.demodu.turbohearts.gamelogic.Card> hand;

	private ArrayList<com.demodu.turbohearts.gamelogic.Card> legalPlays;

	private boolean heartsBroken;

	private ArrayList<com.demodu.turbohearts.gamelogic.Card> chargedCards;
	private ArrayList<com.demodu.turbohearts.gamelogic.Card.Suit> playedSuits;


	public ClientGameView(List<com.demodu.turbohearts.gamelogic.Card> table, List<com.demodu.turbohearts.gamelogic.Card> hand, com.demodu.turbohearts.gamelogic.GameConductor.Phase gamePhase,
						  com.demodu.turbohearts.gamelogic.GameConductor.Round gameRound, boolean heartsBroken, List<com.demodu.turbohearts.gamelogic.Card> legalPlays,
						  List<com.demodu.turbohearts.gamelogic.Card> chargedCards, List<com.demodu.turbohearts.gamelogic.Card.Suit> playedSuits
	) {
		this.table = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(table);
		this.hand = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(hand);
		this.gamePhase = gamePhase;
		this.gameRound = gameRound;
		this.heartsBroken = heartsBroken;
		this.legalPlays = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(legalPlays);
		this.chargedCards = new ArrayList<com.demodu.turbohearts.gamelogic.Card>(chargedCards);
		this.playedSuits = new ArrayList<com.demodu.turbohearts.gamelogic.Card.Suit>(playedSuits);
	}

	public List<com.demodu.turbohearts.gamelogic.Card> getTable() {
		return new ArrayList<com.demodu.turbohearts.gamelogic.Card>(table);
	}

	public List<com.demodu.turbohearts.gamelogic.Card> getHand() {
		return new ArrayList<com.demodu.turbohearts.gamelogic.Card>(hand);
	}

	public boolean isHeartsBroken() {
		return heartsBroken;
	}

	public ArrayList<com.demodu.turbohearts.gamelogic.Card> getLegalPlays() {
		return legalPlays;
	}

	public ArrayList<com.demodu.turbohearts.gamelogic.Card> getChargedCards() {
		return chargedCards;
	}

	public ArrayList<com.demodu.turbohearts.gamelogic.Card.Suit> getPlayedSuits() {
		return playedSuits;
	}

	public com.demodu.turbohearts.gamelogic.GameConductor.Phase getGamePhase() {
		return gamePhase;
	}

	public com.demodu.turbohearts.gamelogic.GameConductor.Round getGameRound() {
		return gameRound;
	}
}
