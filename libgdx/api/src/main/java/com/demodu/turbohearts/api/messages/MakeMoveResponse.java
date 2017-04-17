package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as=ImmutableMakeMoveResponse.class)
@JsonDeserialize(as=ImmutableMakeMoveResponse.class)
public abstract class MakeMoveResponse extends ApiMessage {
	public abstract boolean getSuccess();

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract String getMessage();
}
