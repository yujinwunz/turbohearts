package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.messages.ImmutableMakeMoveResponse;
import com.demodu.turbohearts.api.messages.ImmutablePollGameResponse;
import com.demodu.turbohearts.api.messages.MakeMoveRequest;
import com.demodu.turbohearts.api.messages.PollGameRequest;
import com.demodu.turbohearts.api.messages.PollGameResponse;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.service.game.LiveGame;
import com.demodu.turbohearts.service.game.LiveGameManager;
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

@Path("game")
public class GameResource {

	@Path("poll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void pollGame(PollGameRequest request, @Suspended AsyncResponse response) {
		AuthHelpers.withLoginAndDbAsync(request, response, (UserSession userSession, Session session) -> {
			try {
				JettyServer.liveGameManager.pollEvents(
						request.getGameId(),
						request.getLatestActionNumber(),
						userSession.getUser(),
						(List<PollGameResponse.GameEvent> pendingEvents) -> {
							response.resume(Response.status(200).entity(
									ImmutablePollGameResponse
											.builder()
											.addAllEvents(pendingEvents)
											.build()
									).build()
							);
							return false;
						});
			} catch (LiveGameManager.InvalidGameIdException e) {
				response.resume(Response.status(404).entity("Game id not found"));
			} catch (LiveGame.UserNotInGameException e) {
				response.resume(Response.status(401).entity("User not in game"));
			}
		});
	}

	@Path("move")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response makeMove(MakeMoveRequest request) {
		return AuthHelpers.withLoginAndDb(request, (UserSession userSession, Session session) -> {
			try {
				JettyServer.liveGameManager.playMove(request.getGameId(), userSession.getUser(), request.getMove());
				return Response.status(200).entity(ImmutableMakeMoveResponse
						.builder()
						.success(true)
						.build()
				).build();
			} catch (MoveReporter.InvalidMoveException e) {
				return Response.status(200).entity(ImmutableMakeMoveResponse
						.builder()
						.success(false)
						.message("Invalid move: " + e.getMessage())
						.build()
				).build();
			} catch (LiveGame.UserNotInGameException e) {
				return Response.status(200).entity(ImmutableMakeMoveResponse
						.builder()
						.success(false)
						.message("Invalid user: " + e.getMessage())
						.build()
				).build();
			} catch (LiveGameManager.InvalidGameIdException e) {
				return Response.status(200).entity(ImmutableMakeMoveResponse
						.builder()
						.success(false)
						.message("Invalid game: " + e.getMessage())
						.build()
				).build();
			}
		});
	}
}
