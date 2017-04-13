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
	public abstract List<LobbyRoom> getLobbyList();

	@Value.Immutable
	@JsonSerialize(as=ImmutableLobbyRoom.class)
	@JsonDeserialize(as=ImmutableLobbyRoom.class)
	public static abstract class LobbyRoom {
		public abstract int getId();
		public abstract int getVersion();
		public abstract List<String> getPlayerNames();
		public abstract String getTitle();
	}
}
