package com.demodu.turbohearts.api.endpoints;

import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.LoginRequest;
import com.demodu.turbohearts.api.messages.MakeMoveRequest;
import com.demodu.turbohearts.api.messages.MakeMoveResponse;
import com.demodu.turbohearts.api.messages.PollGameRequest;
import com.demodu.turbohearts.api.messages.PollGameResponse;
import com.demodu.turbohearts.api.messages.ProfileResponse;
import com.demodu.turbohearts.api.messages.RegisterRequest;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.api.messages.RoomResponse;

/**
 * Utility class listing endpoint definitions. Required because static abstract methods don't exist
 * as a limitation of Java.
 */
public class Endpoints {

	public static Endpoint<LoginRequest, ProfileResponse> login =
			Endpoint.create("auth/login", LoginRequest.class, ProfileResponse.class);

	public static Endpoint<RegisterRequest, ProfileResponse> register =
			Endpoint.create("auth/register", RegisterRequest.class, ProfileResponse.class);

	public static Endpoint<LobbyListRequest, LobbyListResponse> lobbyList =
			Endpoint.create("lobby/list", LobbyListRequest.class, LobbyListResponse.class);

	public static class Room {

		public static Endpoint<CreateRoomRequest, RoomResponse> createRoom =
				Endpoint.create("lobby/room/create", CreateRoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> enterRoom =
				Endpoint.create("lobby/room/enter", RoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> pollRoom =
				Endpoint.create("lobby/room/poll", RoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> leaveRoom =
				Endpoint.create("lobby/room/leave", RoomRequest.class, RoomResponse.class);

		public static Endpoint<RoomRequest, RoomResponse> startGame =
				Endpoint.create("lobby/room/start", RoomRequest.class, RoomResponse.class);

	}

	public static class Game {
		public static Endpoint<PollGameRequest, PollGameResponse> pollGame =
				Endpoint.create("game/poll", PollGameRequest.class, PollGameResponse.class);

		public static Endpoint<MakeMoveRequest, MakeMoveResponse> makeMove =
				Endpoint.create("game/move", MakeMoveRequest.class, MakeMoveResponse.class);
	}
}
