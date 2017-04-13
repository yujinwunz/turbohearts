package com.demodu.turbohearts.service.events;

import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as=ImmutableLobbyListUpdate.class)
@JsonDeserialize(as=ImmutableLobbyListUpdate.class)
public abstract class LobbyListUpdate extends Event {
	public abstract int getVersion();
	public abstract List<LobbyListResponse.LobbyGame> getLobbyGames();

	@Override
	public Class<LobbyListUpdate> getEventClass() {
		return LobbyListUpdate.class;
	}
}
