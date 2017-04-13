package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.messages.ImmutableLobbyListResponse;
import com.demodu.turbohearts.api.messages.LobbyListRequest;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.service.events.LobbyListUpdate;
import com.demodu.turbohearts.service.models.LobbyGame;
import com.demodu.turbohearts.service.models.UserSession;

import org.hibernate.Session;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("lobby")
public class LobbyResource {
	private static int version = 0;

	private EventBus eventBus;

	public LobbyResource(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public Class<Resource> getResourceClass() {
		return Resource.class;
	}

	protected static int getVersion() {
		return version;
	}

	protected static synchronized int bumpVersion() {
		version += 1;
		return version;
	}

	public class Resource {

		@Path("list")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public void list(LobbyListRequest request, @Suspended final AsyncResponse asyncResponse) {
			AuthHelpers.withLoginAndDbAsync(request, asyncResponse, (UserSession userSession, Session session) -> {
				eventBus.subscribe(LobbyListUpdate.class, (LobbyListUpdate event) -> {
					if (event.getVersion() > request.getLatestRevision()) {
						asyncResponse.resume(Response.ok(200).entity(
								ImmutableLobbyListResponse
										.builder()
										.lobbyList(event.getLobbyGames())
										.revision(event.getVersion())
										.build()
						).build());
						return false;
					} else {
						return true;
					}
				});
				fireLobbyListEvent(session);

				// The response will be handled by the event queue.
				return null;
			});
		}

		@Path("room")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public void room(RoomRequest request, @Suspended final AsyncResponse asyncResponse) {
			AuthHelpers.withLoginAndDbAsync(request, asyncResponse, (UserSession userSession, Session session) -> {

			});
		}

		private void fireLobbyListEvent(Session session) {

			List<LobbyGame> lobbyGames =
					session.createQuery("from " + LobbyGame.TABLE, LobbyGame.class).list();
		}

	}
}
