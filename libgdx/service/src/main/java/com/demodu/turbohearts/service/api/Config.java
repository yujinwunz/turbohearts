package com.demodu.turbohearts.service.api;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public class Config {
	public final static URL serverDomainName;

	static {
		try {
			serverDomainName = new URL("127.0.0.1");
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Server domain name is not a valid URL.");
		}
	}
}
