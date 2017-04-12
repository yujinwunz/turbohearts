package com.demodu.turbohearts.service.api.messages;

public abstract class AuthenticatedRequest extends ApiMessage {
	public abstract String getAuthToken();
}
