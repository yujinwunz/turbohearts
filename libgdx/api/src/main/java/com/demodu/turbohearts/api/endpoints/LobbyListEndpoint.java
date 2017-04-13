package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;


public class LobbyListEndpoint extends Endpoint<LobbyListRequest, LobbyListResponse> {
	public static final String URL = "lobby/list";
	public static final Class REQUEST_TYPE = LobbyListRequest.class;
	public static final Class RESPONSE_TYPE = LobbyListResponse.class;

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public Class<LobbyListRequest> getRequestType() {
		return REQUEST_TYPE;
	}

	@Override
	public Class<LobbyListResponse> getResponseType() {
		return RESPONSE_TYPE;
	}
}
