package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.messages.ProfileResponse;
import com.demodu.turbohearts.api.messages.RegisterRequest;

/**
 * Created by yujinwunz on 13/04/2017.
 */

public class RegisterEndpoint extends Endpoint<RegisterRequest, ProfileResponse> {
	public static final String URL = "auth/register";
	public static final Class REQUEST_TYPE = RegisterRequest.class;
	public static final Class RESPONSE_TYPE = ProfileResponse.class;

	RegisterEndpoint() {} // Not publicly initializable.

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public Class<RegisterRequest> getRequestType() {
		return REQUEST_TYPE;
	}

	@Override
	public Class<ProfileResponse> getResponseType() {
		return RESPONSE_TYPE;
	}
}
