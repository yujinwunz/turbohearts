package com.demodu.turbohearts.service;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.demodu.turbohearts.service.game.LiveGameManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.ws.rs.core.UriBuilder;

import static com.demodu.turbohearts.service.Global.sessionFactory;

public class JettyServer {

	public static final int BACKGROUND_THREADS = 2;
	public static EventBus eventBus;
	public static LiveGameManager liveGameManager;

	@Parameter(names = "-ssl")
	private boolean sslEnabled = false;

	@Parameter(names = "-keystore-path")
	private String keystorePath = null;

	@Parameter(names = "-keystore-alias")
	private String keystoreAlias = null;

	@Parameter(names = "-keystore-pass")
	private String keystorePass = null;

	@Parameter(names="-hostname")
	private String hostName="http://localhost";

	@Parameter(names="-port")
	private int port=8080;

	@Parameter(names="-jerseylog")
	private String jerseyLog = "jersey.log";

	@Parameter(names="-eventBuslog")
	private String eventBusLog = "eventbus.log";

	public static void main(String args[]) {
		JettyServer main = new JettyServer();

		JCommander jCommander = new JCommander();
		jCommander.addObject(main);
		jCommander.parse(args);

		main.run();
	}

	private void run() {

		setUpHibernate();

		setupEventPipeline();

		setupGameManager();

		SslContextFactory factory = setUpSsl();

		setUpAndStartServer(factory);

	}

	private static void setupGameManager() {
		liveGameManager = new LiveGameManager();
	}

	private void setupEventPipeline() {
		Logger logger = null;

		try {
			FileHandler fh = new FileHandler(eventBusLog);
			logger = Logger.getLogger("event");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setLevel(Level.ALL);

		} catch (IOException ex) {
			System.err.println("Couldn't set up event bus logs");
		}

		eventBus = new EventBus(logger);
		for (int i = 0; i < BACKGROUND_THREADS; i++) {
			eventBus.startWorkerThread();
		}
	}

	private SslContextFactory setUpSsl() {

		if (sslEnabled) {
			SslContextFactory factory = new SslContextFactory();
			factory.setCertAlias(keystoreAlias);
			factory.setKeyStorePath(keystorePath);
			factory.setKeyStorePassword(keystorePass);
			factory.setTrustStorePath(keystorePath);

			return factory;
		} else {
			return null;
		}
	}

	private void setUpAndStartServer(SslContextFactory sslContextFactory) {
		URI baseUri = UriBuilder.fromUri(hostName).port(port).build();
		ResourceConfig config = new ResourceConfig(
				LoginResource.class,
				LobbyResource.class,
				GameResource.class
		);

		config.addProperties(new HashMap<String, Object>() {{
			put("com.sun.jersey.api.json.POJOMappingFeature", true);
		}});

		// This block configure the logger with handler and formatter
		try {
			FileHandler fh = new FileHandler(jerseyLog);
			Logger logger = Logger.getLogger("jetty");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setLevel(Level.ALL);

			config.register(new LoggingFeature(logger, LoggingFeature.Verbosity.PAYLOAD_ANY));
		} catch (IOException ex) {
			System.err.println("Couldn't set up jersey logs");
		}

		Server server = JettyHttpContainerFactory.createServer(baseUri, sslContextFactory, config);

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
