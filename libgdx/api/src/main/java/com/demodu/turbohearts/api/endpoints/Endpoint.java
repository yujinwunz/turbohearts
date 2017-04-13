package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.Config;
import com.demodu.turbohearts.api.messages.ApiMessage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public class Endpoint <Req extends ApiMessage, Res extends ApiMessage> {
	private String url;
	private Class<Req> requestType;
	private Class<Res> responseType;

	public Endpoint(String url, Class<Req> requestType, Class<Res> responseType) {
		this.url = url;
		this.requestType = requestType;
		this.responseType = responseType;
	}

	public String getUrl() {
		return url;
	}

	// Required due to type erasure
	public Class<Req> getRequestType() {
		return requestType;
	}
	public Class<Res> getResponseType() {
		return responseType;
	}

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

	public static <Req extends ApiMessage, Res extends ApiMessage> Endpoint<Req, Res>
		create(String url, Class<Req> requestType, Class<Res> responseType) {

		return new Endpoint<Req, Res>(url, requestType, responseType);
	}
}
