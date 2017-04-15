package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as=ImmutablePollGameResponse.class)
@JsonDeserialize(as=ImmutablePollGameResponse.class)
public abstract class PollGameResponse extends ApiMessage {

	public abstract List<GameEvent> getEvents();

	@Value.Immutable
	@JsonSerialize(as=ImmutableGameEvent.class)
	@JsonDeserialize(as=ImmutableGameEvent.class)
	public static abstract class GameEvent {
		public abstract int getActionNumber();
		public abstract ActionType getActionType();

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public abstract Position getPosition();

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public abstract List<Card> getCards();

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public abstract Integer getSelfScore();

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public abstract Integer getLeftScore();

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public abstract Integer getAcrossScore();

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public abstract Integer getRightScore();

	}

	public enum Position {
		Self,
		Left,
		Across,
		Right
	}

	public enum ActionType {
		MoveRequested,
		PlayCard,
		PassCard,
		ChargeCard,
		TrickEnd,
		RoundEnd,
		GameEnd
	}

	@Value.Immutable
	@JsonSerialize(as=ImmutableCard.class)
	@JsonDeserialize(as=ImmutableCard.class)
	public static abstract class Card {
		public abstract Rank getRank();
		public abstract Suit getSuit();
	}

	public enum Rank {
		TWO,
		THREE,
		FOUR,
		FIVE,
		SIX,
		SEVEN,
		EIGHT,
		NINE,
		TEN,
		JACK,
		QUEEN,
		KING,
		ACE
	}

	public enum Suit {
		Club,
		Diamond,
		Spade,
		Heart
	}
}
