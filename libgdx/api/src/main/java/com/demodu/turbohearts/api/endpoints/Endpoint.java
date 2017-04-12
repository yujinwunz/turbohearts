package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.Config;
import com.demodu.turbohearts.api.messages.ApiMessage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public abstract class Endpoint <Req extends ApiMessage, Res extends ApiMessage> {
	public abstract String getUrl();

	// Required due to type erasure
	public abstract Class<Req> getRequestType();
	public abstract Class<Res> getResponseType();

	public Res send(Req message, String userAgent) throws IOException {
		URL url = new URL(Config.SERVER_DOMAIN_NAME, getUrl());


		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("User-Agent", userAgent);
		conn.setRequestProperty("content-type", "application/json");
		conn.setDoOutput(true);
		Req.objectMapper.writeValue(conn.getOutputStream(), message);

		conn.setRequestMethod("POST");
		if (conn.getResponseCode() == 200) {
			return Res.objectMapper.readValue(conn.getInputStream(), getResponseType());
		} else {
			throw new IOException("Could not read response");
		}
	}
}
