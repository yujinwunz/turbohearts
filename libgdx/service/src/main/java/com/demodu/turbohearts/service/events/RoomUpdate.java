package com.demodu.turbohearts.service.events;

import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

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

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract Integer getGameId();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract List<String> getPlayerNames();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract String getForUserId();

	public abstract UpdateType getType();

	public enum UpdateType {
		Update,
		Delete,
		Start
	}

}
