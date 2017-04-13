package com.demodu.turbohearts.service;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.core.UriBuilder;

import static com.demodu.turbohearts.service.Global.sessionFactory;

/**
 * Created by yujinwunz on 9/04/2017.
 */

public class JettyServer {

	public static final int BACKGROUND_THREADS = 2;
	public static EventPipeline eventPipeline;

	public static void main(String args[]) {
		setUpHibernate();

		setupEventPipeline();

		setUpAndStartServer();
	}

	private static void setupEventPipeline() {
		eventPipeline = new EventPipeline();
		for (int i = 0; i < BACKGROUND_THREADS; i++) {
			eventPipeline.startWorkerThread();
		}
	}

	private static void setUpAndStartServer() {
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

	private static void setUpHibernate() {
		// A SessionFactory is set up once for an application!
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.configure() // configures settings from hibernate.cfg.xml
				.build();
		try {
			sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			StandardServiceRegistryBuilder.destroy( registry );
			e.printStackTrace();
			throw new UnknownError("Couldn't set up hibernate");
		}
	}
}
