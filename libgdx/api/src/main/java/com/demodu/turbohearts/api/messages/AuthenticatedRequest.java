package com.demodu.turbohearts.api.messages;

public abstract class AuthenticatedRequest extends ApiMessage {
	public abstract String getAuthToken();
}
