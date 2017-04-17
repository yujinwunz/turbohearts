package com.demodu.android;

import com.badlogic.gdx.Gdx;
import com.demodu.turbohearts.api.messages.MakeMoveResponse;
import com.demodu.turbohearts.gamelogic.Card;
import com.demodu.turbohearts.gamelogic.GameConductor;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.gamelogic.PlayerActor;
import com.demodu.turbohearts.api.endpoints.Endpoints;
import com.demodu.turbohearts.api.messages.ImmutableMakeMoveRequest;
import com.demodu.turbohearts.api.messages.ImmutablePollGameRequest;
import com.demodu.turbohearts.api.messages.PollGameResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;

public class AndroidGameConductor extends GameConductor {

	private Thread activePollingThread;

	private AndroidAuthManager authManager;
	private int gameId;
	private AndroidLauncher context;
	private int latestEventNumber;

	public AndroidGameConductor(AndroidLauncher context, AndroidAuthManager authManager, int gameId) {
		this.authManager = authManager;
		this.context = context;
		this.gameId = gameId;
	}

	@Override
	public void onRegisterPlayer(final PlayerActor playerActor) {
		activePollingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				latestEventNumber = -1;
				while (true) {
					try {
						PollGameResponse response = Endpoints.Game.pollGame.send(
								ImmutablePollGameRequest.builder()
								.authToken(authManager.getAuthToken())
								.gameId(gameId)
								.latestActionNumber(latestEventNumber)
								.build()
						, context.getString(R.string.user_agent));

						for (final PollGameResponse.GameEvent event : response.getEvents()) {
							switch (event.getEventType()) {
								case MoveRequested:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											playerActor.getMove(Util.toCore(event.getGameView()), new MoveReporter() {
												@Override
												protected void reportMoveImpl(List<Card> move) {
													pushMove(move);
												}
											});
										}
									});
									break;
								case PlayCard:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											playerActor.reportPlay(
													Util.toCore(event.getPosition()),
													Util.toCore(event.getCards().get(0))
											);
										}
									});
									break;
								case PassCards:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											playerActor.reportPass(
													Util.toCore(event.getPosition()),
													Util.toCore(event.getCards())
											);
										}
									});
									break;
								case ChargeCard:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											playerActor.reportCharge(
													Util.toCore(event.getPosition()),
													Util.toCore(event.getCards().get(0))
											);
										}
									});
									break;
								case TrickEnd:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											playerActor.reportTrickEnd(
													Util.toCore(event.getPosition())
											);
										}
									});
									break;
								case RoundEnd:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											//noinspection ConstantConditions
											playerActor.reportRoundEnd(
													event.getSelfScore(),
													event.getLeftScore(),
													event.getAcrossScore(),
													event.getRightScore()
											);
										}
									});
									break;
								case GameEnd:
									context.postRunnable(new Runnable() {
										@Override
										public void run() {
											//noinspection ConstantConditions
											playerActor.reportGameEnd(
													event.getSelfScore(),
													event.getLeftScore(),
													event.getAcrossScore(),
													event.getRightScore()
											);
										}
									});
									break;
							}
							latestEventNumber = event.getEventNumber();
						}
					} catch (IOException ex) {
						Gdx.app.log("AndroidGameConductor", "Polling for game updates failed");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex2) {
							Gdx.app.log("AndroidGameConductor", "Interrupted while cooling down retry");
							break;
						}
					}
				}
			}
		});
		activePollingThread.start();
	}

	private void pushMove(final List<Card> cards) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Gdx.app.log("AndroidGameConductor", "Making move");
					MakeMoveResponse response = Endpoints.Game.makeMove.send(
							ImmutableMakeMoveRequest
									.builder()
									.authToken(authManager.getAuthToken())
									.addAllMove(Util.toApi(cards))
									.gameId(gameId)
									.lastEventNumber(latestEventNumber)
									.build()
					, context.getString(R.string.user_agent));
					try {
						Gdx.app.log("AndroidGameConductor", "Got response " + response.toJsonString());
					} catch (JsonProcessingException ex) {
						Gdx.app.log("AndroidGameConductor", "Couldn't print response " + response);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					Gdx.app.error("AndroidGameConductor", "Could not send move to the server");
					throw new UnknownError("Could not send move to the server"); // TODO: Don't crash.
				}
			}
		});
		t.start();
	}
}
