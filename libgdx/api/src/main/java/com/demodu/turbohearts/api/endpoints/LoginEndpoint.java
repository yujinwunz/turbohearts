package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.messages.LoginRequest;
import com.demodu.turbohearts.api.messages.LoginResponse;


public class LoginEndpoint extends Endpoint<LoginRequest, LoginResponse> {
	public static final String PATH = "auth/login";
	public static final Class requestType = LoginRequest.class;
	public static final Class responseType = LoginResponse.class;

	@Override
	public String getUrl() {
		return PATH;
	}

	@Override
	public Class<LoginRequest> getRequestType() {
		return requestType;
	}

	@Override
	public Class<LoginResponse> getResponseType() {
		return responseType;
	}
}
