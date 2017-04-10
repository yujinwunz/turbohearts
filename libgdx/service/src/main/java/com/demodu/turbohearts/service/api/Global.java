package com.demodu.turbohearts.service.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Collections;


public class Global {
	public static final NetHttpTransport httpTransport = new NetHttpTransport();

	public static final GoogleIdTokenVerifier verifier =
			new GoogleIdTokenVerifier.Builder(
					Global.httpTransport,
					JacksonFactory.getDefaultInstance()
			)
					.setAudience(Collections.singletonList(Config.CLIENT_ID))
					.build();
}
