package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as=ImmutableMakeMoveRequest.class)
@JsonDeserialize(as=ImmutableMakeMoveRequest.class)
public abstract class MakeMoveRequest extends AuthenticatedRequest {
	public abstract int getGameId();
	public abstract List<PollGameResponse.Card> getMove();
}
