package com.demodu.turbohearts.service.api.messages;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public abstract class AuthenticatedRequest extends ApiMessage {
	public abstract String getAuthToken();
}
