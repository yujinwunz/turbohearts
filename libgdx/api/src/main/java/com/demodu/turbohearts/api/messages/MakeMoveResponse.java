package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableMakeMoveResponse.class)
@JsonDeserialize(as=ImmutableMakeMoveResponse.class)
public abstract class MakeMoveResponse extends ApiMessage {
	public abstract boolean getSuccess();
	public abstract String getMessage();
}
