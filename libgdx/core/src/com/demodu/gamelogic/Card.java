package com.demodu.gamelogic;


import com.sun.javafx.UnmodifiableArrayList;

/**
 * Created by yujinwunz on 17/03/2017.
 */

public class Card {

	private Suit suit;
	private Rank rank;

	public static Card example = new Card(Rank.QUEEN, Suit.SPADE);
	private static UnmodifiableArrayList<Card> deck;

	static {
		Card[] cards = new Card[52];
		int i = 0;
		for (Suit s: Suit.values()) {
			for (Rank r: Rank.values()) {
				cards[i] = new Card(r, s);
				i++;
			}
		}
		deck = new UnmodifiableArrayList<Card>(cards, cards.length);
	}

	public static final Card QUEEN_OF_SPADES = new Card(Rank.QUEEN, Suit.SPADE);
	public static final Card JACK_OF_DIAMONDS = new Card(Rank.JACK, Suit.DIAMOND);
	public static final Card TEN_OF_CLUBS = new Card(Rank.TEN, Suit.CLUB);
	public static final Card ACE_OF_HEARTS = new Card(Rank.ACE, Suit.HEART);

	public static UnmodifiableArrayList<Card> getDeck() {
		return deck;
	}

	public Card(Rank rank, Suit suit) {
		this.suit = suit;
		this.rank = rank;
	}

	public Suit getSuit() {
		return suit;
	}

	public Rank getRank() {
		return rank;
	}

	public enum Suit {
		CLUB("c"),
		DIAMOND("d"),
		SPADE("s"),
		HEART("h");

		private String val;

		Suit(String val) {
			this.val = val;
		}

		public String getFilenameVal() {
			return this.val;
		}
	}
	public enum Rank {
		TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"),
		TEN("10"), JACK("j"), QUEEN("q"), KING("k"), ACE("a");

		private String val;

		Rank(String val) {
			this.val = val;
		}
		public String getFilenameVal() {
			return this.val;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Card) {
			return ((Card) other).getRank() == this.getRank()
					&& ((Card) other).getSuit() == this.getSuit();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.getSuit().ordinal() * Rank.values().length + this.getRank().ordinal();
	}
}
