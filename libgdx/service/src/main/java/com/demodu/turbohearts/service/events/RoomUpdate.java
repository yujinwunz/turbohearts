package com.demodu.turbohearts.service.events;

import com.demodu.turbohearts.api.messages.LobbyListResponse;

import org.immutables.value.Value;

@Value.Immutable
public abstract class RoomUpdate extends Event {
	@Override
	public Class<? extends Event> getEventClass() {
		return RoomUpdate.class;
	}

	public abstract LobbyListResponse.LobbyRoom getRoom();
}
