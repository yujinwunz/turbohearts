package com.demodu.turbohearts.api.messages;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as=ImmutableLobbyListResponse.class)
@JsonDeserialize(as=ImmutableLobbyListResponse.class)
public abstract class LobbyListResponse extends ApiMessage {
	public abstract int getRevision();
	public abstract List<LobbyGame> getLobbyList();

	@Value.Immutable
	@JsonSerialize(as=ImmutableLobbyGame.class)
	@JsonDeserialize(as=ImmutableLobbyGame.class)
	public static abstract class LobbyGame {
		public abstract int getId();
		public abstract List<String> getPlayerNames();
		public abstract String getTitle();
	}
}
