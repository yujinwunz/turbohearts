package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutablePollGameRequest.class)
@JsonDeserialize(as=ImmutablePollGameRequest.class)
public abstract class PollGameRequest extends AuthenticatedRequest {
	public abstract String getGameId();
	public abstract String getLatestActionNumber();
}
