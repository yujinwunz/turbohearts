package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableLobbyListResponse;
import com.demodu.turbohearts.api.messages.ImmutableRoomResponse;
import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.api.messages.RoomResponse;
import com.demodu.turbohearts.service.events.Event;
import com.demodu.turbohearts.service.events.ImmutableLobbyListUpdate;
import com.demodu.turbohearts.service.events.ImmutableRoomUpdate;
import com.demodu.turbohearts.service.events.LobbyListUpdate;
import com.demodu.turbohearts.service.events.RoomUpdate;
import com.demodu.turbohearts.service.game.LiveGame;
import com.demodu.turbohearts.service.models.LobbyRoom;
import com.demodu.turbohearts.service.models.User;
import com.demodu.turbohearts.service.models.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.hibernate.LockMode;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.LockModeType;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("lobby")
public class LobbyResource {
	private static int version = 0;

	protected static int getVersion() {
		return version;
	}

	protected static synchronized int bumpVersion() {
		version += 1;
		return version;
	}

	@POST
	@Path("list")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void list(LobbyListRequest request, @Suspended final AsyncResponse asyncResponse) {

		System.out.println("Got lobby list request");

		AuthHelpers.withLoginAndDbAsync(request, asyncResponse, (UserSession userSession, Session session) -> {
			JettyServer.eventBus.subscribe(LobbyListUpdate.class, (LobbyListUpdate event) -> {
				if (event.getVersion() > request.getLatestRevision()) {
					asyncResponse.resume(Response.ok(200).entity(
							ImmutableLobbyListResponse
									.builder()
									.lobbyList(event.getLobbyRooms())
									.revision(event.getVersion())
									.build()
					).build());
					return false;
				} else {
					return true;
				}
			});
			fireLobbyListEvent(getLobbyRooms(session));
		});
	}

	@POST
	@Path("room/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createRoom(CreateRoomRequest request) {
		return AuthHelpers.withLoginAndDb(request, (UserSession userSession, Session session) -> {
			LobbyRoom lobbyRoom = new LobbyRoom(
					request.getName(),
					Collections.singleton(userSession.getUser()),
					0,
					userSession.getUser()
			);
			session.save(lobbyRoom);

			bumpVersion();

			fireLobbyListEvent(getLobbyRooms(session));

			return Response.status(200).entity(
					ImmutableRoomResponse
							.builder()
							.updateType(RoomResponse.UpdateType.EnteredRoom)
							.room(lobbyRoom.toApi())
							.build()
			).build();
		});
	}

	@POST
	@Path("room/enter")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response enterRoom(RoomRequest request) {
		return AuthHelpers.withLoginAndDb(request, (UserSession userSession, Session session) -> {
			List<LobbyRoom> lobbyRooms = getLobbyRooms(session);
			LobbyRoom lobbyRoom = getLobbyRoom(request.getRoomId(), session);

			if (lobbyRoom == null) {
				return Response.status(404).entity("Room does not exist").build();
			}

			lobbyRoom.getPlayers().add(userSession.getUser());
			lobbyRoom.setVersion(lobbyRoom.getVersion() + 1);
			session.update(lobbyRoom);

			bumpVersion();

			fireRoomUpdateEvent(lobbyRoom, session);

			fireLobbyListEvent(lobbyRooms);
			return Response.status(200).entity(
					ImmutableRoomResponse
							.builder()
							.updateType(RoomResponse.UpdateType.EnteredRoom)
							.room(lobbyRoom.toApi())
							.build()
			).build();
		});
	}

	@POST
	@Path("room/leave")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response leaveRoom(RoomRequest request) {
		return AuthHelpers.withLoginAndDb(request, (UserSession userSession, Session session) -> {
			List<LobbyRoom> lobbyRooms = getLobbyRooms(session);
			LobbyRoom lobbyRoom = getLobbyRoom(request.getRoomId(), session);

			if (lobbyRoom == null) {
				return Response.status(404).entity("Room does not exist").build();
			}

			if (!lobbyRoom.hasPlayer(userSession.getUser())) {
				return Response.status(403).entity("You are not in the room").build();
			}

			lobbyRoom.removePlayer(userSession.getUser());
			lobbyRoom.setVersion(lobbyRoom.getVersion() + 1);

			bumpVersion();

			if (lobbyRoom.getPlayers().size() == 0 ||
					lobbyRoom.getHost().equals(userSession.getUser())) {
				session.delete(lobbyRoom);
				lobbyRooms.removeIf((LobbyRoom l) ->
						l.getId() == lobbyRoom.getId());
				fireRoomDeleteEvent(lobbyRoom, session);
			} else {
				session.update(lobbyRoom);
				fireRoomUpdateEvent(lobbyRoom, session);
			}

			fireLobbyListEvent(lobbyRooms);
			return Response.status(200).entity(
					ImmutableRoomResponse
							.builder()
							.updateType(RoomResponse.UpdateType.LeaveRoom)
							.leaveMessage("You have left the room")
							.build()
			).build();
		});
	}

	@POST
	@Path("room/start")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startGame(RoomRequest request) {
		return AuthHelpers.withLoginAndDb(request, (UserSession userSession, Session session) -> {
			LobbyRoom room = getLobbyRoom(request.getRoomId(), session);
			if (room == null) {
				return Response.status(404).entity("Room does not exist").build();
			} else if (!room.getHost().equals(userSession.getUser())) {
				return Response.status(403)
						.entity("You must be the host of the room to start the game").build();
			} else if (room.getPlayers().size() != 4)  {
				return Response.status(422)
						.entity("There must be four players to start the game").build();
			} else {
				room.setGame(JettyServer.liveGameManager.newGame(session, room.getPlayers()).getDbEntry());
				room.setVersion(room.getVersion()+1);
				session.update(room);
				fireStartGameEvent(room, session);
				try {
					return Response.status(200).entity(
							ImmutableRoomResponse
									.builder()
									.updateType(RoomResponse.UpdateType.StartGame)
									.room(room.toApi())
									.addAllGamePlayers(room.getGame().getNameListFromPerspective(userSession.getUser()))
									.gameId(room.getGame().getId())
									.build()
					).build();
				}catch (LiveGame.UserNotInGameException ex) {
					return Response.status(403)
						.entity("You are not in the game").build();

				}
			}
		});
	}

	@POST
	@Path("room/poll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void pollRoom(RoomRequest request, @Suspended final AsyncResponse response) {
		AuthHelpers.withLoginAndDbAsync(request, response, (UserSession userSession, Session session) -> {
			LobbyRoom room = getLobbyRoom(request.getRoomId(), session);

			if (room == null) {
				response.resume(Response.status(404).entity("Room does not exist").build());
			} else if (room.hasPlayer(userSession.getUser()) == false) {
				response.resume(Response.status(403).entity("You're not in the room").build());
			} else {

				if (room.getVersion() > request.getLatestVersion()) {
					if (room.getGame() != null) {
						try {
							response.resume(Response.status(200).entity(
									ImmutableRoomResponse
											.builder()
											.updateType(RoomResponse.UpdateType.StartGame)
											.room(room.toApi())
											.gameId(room.getGame().getId())
											.addAllGamePlayers(room.getGame().getNameListFromPerspective(userSession.getUser()))
											.build()
							).build());

						} catch (LiveGame.UserNotInGameException ex) {
							response.resume(Response.status(403).entity("You're not in the room").build());
						}
					} else {
						response.resume(Response.status(200).entity(
								ImmutableRoomResponse
										.builder()
										.updateType(RoomResponse.UpdateType.UpdateRoom)
										.room(room.toApi())
										.build()
						).build());
					}
				} else {
					JettyServer.eventBus.subscribe(RoomUpdate.class, (RoomUpdate event) -> {
						if (event.getRoom().getId() != request.getRoomId()) {
							return true;
						}
						switch (event.getType()) {
							case Update:
								if (event.getRoom().getVersion() > request.getLatestVersion()) {
									response.resume(Response.status(200).entity(
											ImmutableRoomResponse
													.builder()
													.updateType(RoomResponse.UpdateType.UpdateRoom)
													.room(event.getRoom())
													.build()
									).build());
									return false;
								} else {
									return true;
								}
							case Delete:
								response.resume(Response.status(200).entity(
										ImmutableRoomResponse
												.builder()
												.updateType(RoomResponse.UpdateType.LeaveRoom)
												.leaveMessage("The room was closed")
												.build()
								).build());
								return false;
							case Start:
								try {
									System.out.println("Received start event " + Event.objectMapper.writeValueAsString(event) + ". I am " + userSession.getUser().getId());
								} catch (JsonProcessingException ex) {
									System.out.println("Got start event but was unable to parse it");
								}
								if (event.getForUserId().equals(userSession.getUser().getId())) {
									if (event.getRoom().getVersion() > request.getLatestVersion()) {
										System.out.println("Sending response");
										response.resume(Response.status(200).entity(
												ImmutableRoomResponse
														.builder()
														.updateType(RoomResponse.UpdateType.StartGame)
														.room(event.getRoom())
														.addAllGamePlayers(event.getPlayerNames())
														.gameId(event.getGameId())
														.build()
										).build());
									}
									return false;
								} else {
									return true;
								}
						}
						return true;
					});
				}
			}
		});
	}

	private LobbyRoom getLobbyRoom(int roomId, Session session) {
		try {
			return session.load(LobbyRoom.class, roomId, LockMode.PESSIMISTIC_WRITE);
		} catch (ObjectNotFoundException ex) {
			return null;
		}
	}

	private void fireRoomUpdateEvent(LobbyRoom room, Session session) {
		JettyServer.eventBus.fireEvent(ImmutableRoomUpdate.builder()
				.room(room.toApi())
				.type(RoomUpdate.UpdateType.Update)
				.build()
		);
	}

	private void fireRoomDeleteEvent(LobbyRoom room, Session session) {
		JettyServer.eventBus.fireEvent(ImmutableRoomUpdate.builder()
				.room(room.toApi())
				.type(RoomUpdate.UpdateType.Delete)
				.build()
		);
	}

	private void fireStartGameEvent(LobbyRoom room, Session session) {
		for (User u :room.getPlayers()) {
			try {
				JettyServer.eventBus.fireEvent(ImmutableRoomUpdate.builder()
						.room(room.toApi())
						.type(RoomUpdate.UpdateType.Start)
						.playerNames(room.getGame().getNameListFromPerspective(u))
						.forUserId(u.getId())
						.gameId(room.getGame().getId())
						.build()
				);
			} catch (LiveGame.UserNotInGameException ex) {
				throw new UnknownError("User list in game does not match user list in lobby");
			}
		}
	}

	private List<LobbyRoom> getLobbyRooms(Session session) {
		return session.createQuery("from LobbyRoom", LobbyRoom.class)
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.list();
	}

	private synchronized void fireLobbyListEvent(List<LobbyRoom> lobbyRooms) {
		List<LobbyListResponse.LobbyRoom> apiList = new ArrayList<>();
		for (LobbyRoom room : lobbyRooms) {
			apiList.add(room.toApi());
		}
		JettyServer.eventBus.fireEvent(ImmutableLobbyListUpdate
				.builder()
				.version(getVersion())
				.lobbyRooms(apiList)
				.build()
		);
	}
}
