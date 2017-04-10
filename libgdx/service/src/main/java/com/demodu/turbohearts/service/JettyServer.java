package com.demodu.turbohearts.service;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.core.UriBuilder;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public class JettyServer {
	public static void main(String args[]) {
		URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
		ResourceConfig config = new ResourceConfig(
				LoginResource.class
		);
		config.addProperties(new HashMap<String, Object>() {{
			put("com.sun.jersey.api.json.POJOMappingFeature", true);
		}});

		Server server = JettyHttpContainerFactory.createServer(baseUri, config);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			throw new UnknownError("Could not start the jetty server");
		}
	}
}
