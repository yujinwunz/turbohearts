package com.demodu.turbohearts.service.api.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as=ImmutableLoginResponse.class)
@JsonDeserialize(as=ImmutableLoginResponse.class)
public abstract class LoginResponse extends ApiMessage {

	public abstract boolean getSuccess();

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Nullable
	public abstract String getErrorMessage();

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Nullable
	public abstract String getUsername();

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Nullable
	public abstract String displayName();

}
