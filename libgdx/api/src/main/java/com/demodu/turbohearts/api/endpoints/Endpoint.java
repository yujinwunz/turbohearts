package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.Config;
import com.demodu.turbohearts.api.messages.ApiMessage;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public class Endpoint <Req extends ApiMessage, Res extends ApiMessage> {
	public static int MAX_RETRIES = 5;
	public static int RETRY_COOLDOWN_MS = 1000;
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

		for (int retries = 0; retries < MAX_RETRIES; retries++) {
			try {
				if (conn.getResponseCode() == 200) {
					return Res.objectMapper.readValue(conn.getInputStream(), getResponseType());
				} else {
					System.out.println(conn.getResponseCode());
					System.out.println(conn.getResponseMessage());
					break;
				}
			} catch (IOException ex) {
				try {
					Thread.sleep(RETRY_COOLDOWN_MS);
				} catch (InterruptedException ex2) {
					throw new InterruptedIOException("Interrupted while retrying");
				}
			}
		}

		throw new IOException("Could not read response");
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static <Req extends ApiMessage, Res extends ApiMessage> Endpoint<Req, Res>
		create(String url, Class<Req> requestType, Class<Res> responseType) {

		return new Endpoint<Req, Res>(url, requestType, responseType);
	}
}
