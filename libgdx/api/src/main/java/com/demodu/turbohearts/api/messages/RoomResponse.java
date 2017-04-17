package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as=ImmutableRoomResponse.class)
@JsonDeserialize(as=ImmutableRoomResponse.class)
public abstract class RoomResponse extends ApiMessage {

	public abstract UpdateType getUpdateType();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract LobbyListResponse.LobbyRoom getRoom();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract Integer getGameId();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract String getLeaveMessage();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract List<String> getGamePlayers();

	public enum UpdateType {
		EnteredRoom,
		UpdateRoom,
		StartGame,
		LeaveRoom
	}
}
