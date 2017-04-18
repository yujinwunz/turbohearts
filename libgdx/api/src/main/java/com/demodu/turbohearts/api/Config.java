package com.demodu.turbohearts.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class  Config {
	public final static URL SERVER_DOMAIN_NAME;

	/**
	 * For the android app
	 */
	public final static String CLIENT_ID = "643321425618-8s4k5gi0vd3m9ve7bjv0pnvd7eqerlsv.apps.googleusercontent.com";

	public final static List<String> CLIENT_ID_LIST = new ArrayList<String>() {{
		add(CLIENT_ID);
	}};

	static {
		try {
			SERVER_DOMAIN_NAME = new URL("https://demodu.com");
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Server domain name is not a valid URL.");
		}
	}
}
