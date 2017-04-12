package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.messages.LoginRequest;
import com.demodu.turbohearts.api.messages.ProfileResponse;


public class LoginEndpoint extends Endpoint<LoginRequest, ProfileResponse> {
	public static final String PATH = "auth/login";
	public static final Class REQUEST_TYPE = LoginRequest.class;
	public static final Class RESPONSE_TYPE = ProfileResponse.class;

	LoginEndpoint() {} // Not publicly initializable.

	@Override
	public String getUrl() {
		return PATH;
	}

	@Override
	public Class<LoginRequest> getRequestType() {
		return REQUEST_TYPE;
	}

	@Override
	public Class<ProfileResponse> getResponseType() {
		return RESPONSE_TYPE;
	}
}
