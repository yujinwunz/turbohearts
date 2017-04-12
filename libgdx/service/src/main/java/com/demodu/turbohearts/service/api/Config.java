package com.demodu.turbohearts.service.api;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public class Config {
	public final static URL SERVER_DOMAIN_NAME;

	/**
	 * For the android app
	 */
	public final static String CLIENT_ID = "643321425618-0qe4d2mihfu5e1ut25o1b1c7ot04tgj2.apps.googleusercontent.com";

	static {
		try {
			SERVER_DOMAIN_NAME = new URL("192.168.1.4");
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Server domain name is not a valid URL.");
		}
	}
}
