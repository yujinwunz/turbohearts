package com.demodu.turbohearts.api.messages;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableRegisterRequest.class)
@JsonDeserialize(as=ImmutableRegisterRequest.class)
public abstract class RegisterRequest extends AuthenticatedRequest {
	public abstract String getUserName();
	public abstract String getDisplayName();
}
