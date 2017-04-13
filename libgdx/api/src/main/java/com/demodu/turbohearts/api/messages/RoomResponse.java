package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;

import org.immutables.value.Value;

import javax.annotation.Nullable;

/**
 * Created by yujinwunz on 13/04/2017.
 */

@Value.Immutable
@JsonSerialize(as=ImmutableRoomResponse.class)
@JsonDeserialize(as=ImmutableRoomResponse.class)
public abstract class RoomResponse extends ApiMessage {

	public abstract UpdateType getUpdateType();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract Room getRoom();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract String getGameId();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract String getLeaveMessage();

	public enum UpdateType {
		EnteredRoom,
		UpdateRoom,
		StartGame,
		LeaveRoom
	}

	@Value.Immutable
	@JsonSerialize(as=ImmutableRoom.class)
	@JsonDeserialize(as=ImmutableRoom.class)
	public static abstract class Room {
		public abstract int getId();
		public abstract int getVersion();
		public abstract List<String> getPlayers();
	}
}
