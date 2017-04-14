package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableLobbyListResponse;
import com.demodu.turbohearts.api.messages.ImmutableRoomResponse;
import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.api.messages.RoomResponse;
import com.demodu.turbohearts.service.events.ImmutableLobbyListUpdate;
import com.demodu.turbohearts.service.events.ImmutableRoomUpdate;
import com.demodu.turbohearts.service.events.LobbyListUpdate;
import com.demodu.turbohearts.service.events.RoomUpdate;
import com.demodu.turbohearts.service.models.LobbyRoom;
import com.demodu.turbohearts.service.models.ModelLocks;
import com.demodu.turbohearts.service.models.UserSession;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

	@GET
	@Path("Test")
	public Response test() {
		return Response.ok().entity("Hello").build();
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
			fireLobbyListEvent(session);
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

			fireLobbyListEvent(session);

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
			synchronized (ModelLocks.LobbyRoom.getLock(request.getRoomId())) {
				LobbyRoom lobbyRoom = getLobbyRoom(request.getRoomId(), session);

				if (lobbyRoom == null) {
					return Response.status(200).entity(
							ImmutableRoomResponse
									.builder()
									.updateType(RoomResponse.UpdateType.LeaveRoom)
									.room(null)
									.leaveMessage("Room does not exist")
									.build()
					).build();
				}

				lobbyRoom.getPlayers().add(userSession.getUser());
				lobbyRoom.setVersion(lobbyRoom.getVersion() + 1);
				session.update(lobbyRoom);

				bumpVersion();

				fireRoomUpdateEvent(lobbyRoom, session);

				fireLobbyListEvent(session);
				return Response.status(200).entity(
						ImmutableRoomResponse
								.builder()
								.updateType(RoomResponse.UpdateType.EnteredRoom)
								.room(lobbyRoom.toApi())
								.build()
				).build();
			}
		});
	}

	@POST
	@Path("room/leave")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response leaveRoom(RoomRequest request) {
		return AuthHelpers.withLoginAndDb(request, (UserSession userSession, Session session) -> {
			synchronized (ModelLocks.LobbyRoom.getLock(request.getRoomId())) {
				LobbyRoom lobbyRoom = getLobbyRoom(request.getRoomId(), session);

				if (lobbyRoom == null) {
					return Response.status(200).entity(
							ImmutableRoomResponse
									.builder()
									.updateType(RoomResponse.UpdateType.LeaveRoom)
									.room(null)
									.leaveMessage("Room does not exist")
									.build()
					).build();
				}

				if (!lobbyRoom.hasPlayer(userSession.getUser())) {
					return Response.status(200).entity(
							ImmutableRoomResponse
									.builder()
									.updateType(RoomResponse.UpdateType.LeaveRoom)
									.room(null)
									.leaveMessage("You are not in the room")
									.build()
					).build();
				}

				lobbyRoom.removePlayer(userSession.getUser());
				lobbyRoom.setVersion(lobbyRoom.getVersion() + 1);

				bumpVersion();

				if (lobbyRoom.getPlayers().size() == 0 ||
						lobbyRoom.getHost().equals(userSession.getUser())) {
					session.delete(lobbyRoom);
					fireRoomDeleteEvent(lobbyRoom, session);
				} else {
					session.update(lobbyRoom);
					fireRoomUpdateEvent(lobbyRoom, session);
				}

				fireLobbyListEvent(session);
				return Response.status(200).entity(
						ImmutableRoomResponse
								.builder()
								.updateType(RoomResponse.UpdateType.LeaveRoom)
								.leaveMessage("You have left the room")
								.build()
				).build();
			}
		});
	}

	@POST
	@Path("room/poll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void pollRoom(RoomRequest request, @Suspended final AsyncResponse response) {
		AuthHelpers.withLoginAndDbAsync(request, response, (UserSession userSession, Session session) -> {
			synchronized (ModelLocks.LobbyRoom.getLock(request.getRoomId())) {

				LobbyRoom room = getLobbyRoom(request.getRoomId(), session);

				if (room == null) {
					response.resume(Response.status(200).entity(
							ImmutableRoomResponse
									.builder()
									.updateType(RoomResponse.UpdateType.LeaveRoom)
									.room(null)
									.leaveMessage("Room does not exist")
					).build());
				} else if (room.hasPlayer(userSession.getUser()) == false) {
					response.resume(Response.status(200).entity(
							ImmutableRoomResponse
									.builder()
									.updateType(RoomResponse.UpdateType.LeaveRoom)
									.room(null)
									.leaveMessage("You're not in the room")
					).build());
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
						}
						return false;
					});
				}
			}
		});
	}

	private LobbyRoom getLobbyRoom(int roomId, Session session) {
		List<LobbyRoom> lobbyRoomList =
				session.createQuery("from LobbyRoom where id=?", LobbyRoom.class).setParameter(0, roomId).list();
		if (lobbyRoomList.size() == 0) {
			return null;
		}
		return lobbyRoomList.get(0);
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

	private synchronized void fireLobbyListEvent(Session session) {
		List<LobbyRoom> lobbyRooms =
				session.createQuery("from LobbyRoom", LobbyRoom.class).list();
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
