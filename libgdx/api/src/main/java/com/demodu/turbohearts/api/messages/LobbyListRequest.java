package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableLobbyListRequest.class)
@JsonDeserialize(as=ImmutableLobbyListRequest.class)
public abstract class LobbyListRequest extends AuthenticatedRequest {
	public abstract int getLatestRevision();
}
