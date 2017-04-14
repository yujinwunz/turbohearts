package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.LoginRequest;
import com.demodu.turbohearts.api.messages.ProfileResponse;
import com.demodu.turbohearts.api.messages.RegisterRequest;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.api.messages.RoomResponse;

/**
 * Utility class listing endpoint definitions. Required because static abstract methods don't exist
 * as a limitation of Java.
 */
public class Endpoints {

	public static Endpoint<LoginRequest, ProfileResponse> loginEndpoint =
			Endpoint.create("auth/login", LoginRequest.class, ProfileResponse.class);

	public static Endpoint<RegisterRequest, ProfileResponse> registerEndpoint =
			Endpoint.create("auth/register", RegisterRequest.class, ProfileResponse.class);

	public static Endpoint<LobbyListRequest, LobbyListResponse> lobbyListEndpoint =
			Endpoint.create("lobby/list", LobbyListRequest.class, LobbyListResponse.class);

	public static class Room {

		public static Endpoint<CreateRoomRequest, RoomResponse> createRoomEndpoint =
				Endpoint.create("lobby/room/create", CreateRoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> enterRoomEndpoint =
				Endpoint.create("lobby/room/enter", RoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> pollRoomEndpoint =
				Endpoint.create("lobby/room/poll", RoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> leaveRoomEndpoint =
				Endpoint.create("lobby/room/leave", RoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> startGameEndpoint =
				Endpoint.create("lobby/room/start", RoomRequest.class, RoomResponse.class);

	}
}
