package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.Config;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.hibernate.SessionFactory;


public class Global {
	public static final NetHttpTransport httpTransport = new NetHttpTransport();

	public static SessionFactory sessionFactory;

	public static final GoogleIdTokenVerifier verifier =
			new GoogleIdTokenVerifier.Builder(
					Global.httpTransport,
					JacksonFactory.getDefaultInstance()
			)
					.setAudience(Config.CLIENT_ID_LIST)
					.build();
}
