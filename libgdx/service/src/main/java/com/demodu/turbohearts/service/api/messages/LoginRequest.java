package com.demodu.turbohearts.service.api.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableLoginRequest.class)
@JsonDeserialize(as=ImmutableLoginRequest.class)
public abstract class LoginRequest extends ApiMessage {
	public abstract String getIdToken();
}
