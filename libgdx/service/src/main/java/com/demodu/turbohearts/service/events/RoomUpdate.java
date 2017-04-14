package com.demodu.turbohearts.service.events;

import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as=ImmutableRoomUpdate.class)
@JsonDeserialize(as=ImmutableRoomUpdate.class)
public abstract class RoomUpdate extends Event {
	@Override
	public Class<? extends Event> getEventClass() {
		return RoomUpdate.class;
	}

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract LobbyListResponse.LobbyRoom getRoom();

	public abstract UpdateType getType();

	public enum UpdateType {
		Update,
		Delete
	}
}
