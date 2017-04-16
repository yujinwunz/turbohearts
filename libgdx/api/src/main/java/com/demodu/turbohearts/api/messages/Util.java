package com.demodu.turbohearts.api.messages;

import com.demodu.turbohearts.gamelogic.Card;
import com.demodu.turbohearts.gamelogic.ClientGameView;
import com.demodu.turbohearts.gamelogic.GameConductor;

import java.util.ArrayList;
import java.util.List;

public class Util {

	public static Card toCore(PollGameResponse.Card api) {
		return new Card(toCore(api.getRank()), toCore(api.getSuit()));
	}

	public static PollGameResponse.Card toApi(Card core) {
		return ImmutableCard
				.builder()
				.rank(toApi(core.getRank()))
				.suit(toApi(core.getSuit()))
				.build();
	}

	public static ClientGameView toCore(PollGameResponse.GameView api) {
		return new ClientGameView(
				toCore(api.getTable()),
				toCore(api.getHand()),
				toCore(api.getGamePhase()),
				toCoreRound(api.getGameRound()),
				api.getHeartsBroken(),
				toCore(api.getLegalPlays()),
				toCore(api.getChargedCards()),
				toCoreSuit(api.getPlayedSuits())
		);
	}

	public static PollGameResponse.GameView toApi(ClientGameView core) {
		return ImmutableGameView
				.builder()
				.addAllHand(toApi(core.getHand()))
				.addAllChargedCards(toApi(core.getChargedCards()))
				.addAllLegalPlays(toApi(core.getLegalPlays()))
				.addAllPlayedSuits(toApiSuit(core.getPlayedSuits()))
				.addAllTable(toApi(core.getTable()))
				.gamePhase(toApi(core.getGamePhase()))
				.gameRound(toApiRound(core.getGameRound()))
				.heartsBroken(core.isHeartsBroken())
				.build();
	}

	public static List<Card> toCore(List<PollGameResponse.Card> api) {
		List<Card> ret = new ArrayList<>();
		for (PollGameResponse.Card c : api) {
			ret.add(toCore(c));
		}
		return ret;
	}

	public static List<PollGameResponse.Card> toApi(List<Card> core) {
		List<PollGameResponse.Card> ret = new ArrayList<>();
		for (Card c : core) {
			ret.add(toApi(c));
		}
		return ret;
	}

	public static List<Card.Suit> toCoreSuit(List<PollGameResponse.Suit> api) {
		List<Card.Suit> ret = new ArrayList<>();
		for (PollGameResponse.Suit s : api) {
			ret.add(toCore(s));
		}
		return ret;
	}

	public static List<PollGameResponse.Suit> toApiSuit(List<Card.Suit> core) {
		List<PollGameResponse.Suit> ret = new ArrayList<>();
		for (Card.Suit s : core) {
			ret.add(toApi(s));
		}
		return ret;
	}

	public static GameConductor.Phase toCore(PollGameResponse.Phase api) {
		switch (api) {
			case Ready:
				return GameConductor.Phase.Ready;
			case Passing:
				return GameConductor.Phase.Passing;
			case Charging:
				return GameConductor.Phase.Charging;
			case FirstRound:
				return GameConductor.Phase.FirstRound;
			case Playing:
				return GameConductor.Phase.Playing;
			case Finished:
				return GameConductor.Phase.Finished;
			default:
				throw new IllegalArgumentException("Invalid game phase");
		}
	}

	public static PollGameResponse.Phase toApi(GameConductor.Phase api) {
		switch (api) {
			case Ready:
				return PollGameResponse.Phase.Ready;
			case Passing:
				return PollGameResponse.Phase.Passing;
			case Charging:
				return PollGameResponse.Phase.Charging;
			case FirstRound:
				return PollGameResponse.Phase.FirstRound;
			case Playing:
				return PollGameResponse.Phase.Playing;
			case Finished:
				return PollGameResponse.Phase.Finished;
			default:
				throw new IllegalArgumentException("Invalid game phase");
		}
	}

	public static GameConductor.Round toCoreRound(int api) {
		switch (api%4) {
			case 0:
				return GameConductor.Round.Left;
			case 1:
				return GameConductor.Round.Across;
			case 2:
				return GameConductor.Round.Right;
			default:
				return GameConductor.Round.NoPass;
		}
	}

	public static int toApiRound(GameConductor.Round core) {
		switch (core) {
			case Left:
				return 0;
			case Across:
				return 1;
			case Right:
				return 2;
			case NoPass:
				return 3;
			default:
				throw new IllegalArgumentException("Not a valid round");
		}
	}

	private static Card.Suit[] coreSuit = Card.Suit.values();
	private static Card.Rank[] coreRank = Card.Rank.values();

	private static PollGameResponse.Suit[] apiSuit = PollGameResponse.Suit.values();
	private static PollGameResponse.Rank[] apiRank = PollGameResponse.Rank.values();

	public static Card.Suit toCore(PollGameResponse.Suit api) {
		return coreSuit[api.ordinal()];
	}

	public static Card.Rank toCore(PollGameResponse.Rank api) {
		return coreRank[api.ordinal()];
	}

	public static PollGameResponse.Suit toApi(Card.Suit api) {
		return apiSuit[api.ordinal()];
	}

	public static PollGameResponse.Rank toApi(Card.Rank api) {
		return apiRank[api.ordinal()];
	}


	public static GameConductor.PlayerPosition toCore(PollGameResponse.Position api) {
		switch (api) {
			case Self:
				return GameConductor.PlayerPosition.Self;
			case Left:
				return GameConductor.PlayerPosition.Left;
			case Across:
				return GameConductor.PlayerPosition.Across;
			case Right:
				return GameConductor.PlayerPosition.Right;
			default:
				throw new IllegalArgumentException("Invalid Position");
		}
	}

	public static PollGameResponse.Position toApi(GameConductor.PlayerPosition core) {
		switch (core) {
			case Self:
				return PollGameResponse.Position.Self;
			case Left:
				return PollGameResponse.Position.Left;
			case Across:
				return PollGameResponse.Position.Across;
			case Right:
				return PollGameResponse.Position.Right;
			default:
				throw new IllegalArgumentException("Invalid Position");
		}
	}
}
