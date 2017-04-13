package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableCreateRoomRequest.class)
@JsonDeserialize(as=ImmutableCreateRoomRequest.class)
public abstract class CreateRoomRequest extends AuthenticatedRequest {
	public abstract String getName();
}
