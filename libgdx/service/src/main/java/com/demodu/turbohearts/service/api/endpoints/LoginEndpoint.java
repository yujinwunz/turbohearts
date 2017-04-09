package com.demodu.turbohearts.service.api.endpoints;

import com.demodu.turbohearts.service.api.messages.LoginRequest;
import com.demodu.turbohearts.service.api.messages.LoginResponse;


public class LoginEndpoint extends Endpoint<LoginRequest, LoginResponse> {
	public static final String PATH = "auth/login";

	@Override
	public String getUrl() {
		return PATH;
	}

	@Override
	public Class<LoginRequest> getRequestType() {
		return LoginRequest.class;
	}

	@Override
	public Class<LoginResponse> getResponseType() {
		return LoginResponse.class;
	}
}
