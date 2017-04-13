package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as=ImmutableRoomRequest.class)
@JsonDeserialize(as=ImmutableRoomRequest.class)
public abstract class RoomRequest extends AuthenticatedRequest {
	public abstract Integer getRoomId();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract Integer getLatestVersion();
}
