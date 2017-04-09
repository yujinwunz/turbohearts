package com.demodu.turbohearts.service.api.messages;

import org.immutables.value.Value;


@Value.Immutable
public abstract class LoginRequest extends AuthenticatedRequest {
	public abstract String getTestVal();
}
