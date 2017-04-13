package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableLobbyListResponse;
import com.demodu.turbohearts.api.messages.ImmutableRoomResponse;
import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.RoomResponse;
import com.demodu.turbohearts.service.events.ImmutableLobbyListUpdate;
import com.demodu.turbohearts.service.events.LobbyListUpdate;
import com.demodu.turbohearts.service.models.LobbyRoom;
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
	public void createRoom(CreateRoomRequest request, @Suspended final AsyncResponse asyncResponse) {
		AuthHelpers.withLoginAndDbAsync(request, asyncResponse, (UserSession userSession, Session session) -> {
			LobbyRoom lobbyRoom = new LobbyRoom(request.getName(), Collections.emptySet(), 0);
			session.save(lobbyRoom);

			asyncResponse.resume(Response.status(200).entity(
					ImmutableRoomResponse
							.builder()
							.updateType(RoomResponse.UpdateType.EnteredRoom)
							.room(lobbyRoom.toApi())
							.build()
			));

			session.getTransaction().commit();

			fireLobbyListEvent(session);
		});
	}

	private void fireLobbyListEvent(Session session) {

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
