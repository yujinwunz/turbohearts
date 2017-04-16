package com.demodu.turbohearts.api.messages;

import org.immutables.value.Value;

@Value.Immutable
public abstract class StartGameRequest extends AuthenticatedRequest {
	public abstract int getGameId();
}
