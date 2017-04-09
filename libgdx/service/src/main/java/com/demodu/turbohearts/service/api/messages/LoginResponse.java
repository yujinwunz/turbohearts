package com.demodu.turbohearts.service.api.messages;

import org.immutables.value.Value;

@Value.Immutable
public abstract class LoginResponse extends ApiMessage {
	public abstract boolean getSuccess();
	public abstract String getErrorMessage();
	public abstract String getUsername();
	public abstract String displayName();
}
