package com.demodu.turbohearts.api.endpoints;

/**
 * Utility class listing endpoint definitions. Required because static abstract methods don't exist
 * as a limitation of Java.
 */
public class Endpoints {
	public static LoginEndpoint loginEndpoint = new LoginEndpoint();
	public static RegisterEndpoint registerEndpoint = new RegisterEndpoint();
	public static LobbyListEndpoint lobbyListEndpoint = new LobbyListEndpoint();
}
