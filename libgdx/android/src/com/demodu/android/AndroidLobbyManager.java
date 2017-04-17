package com.demodu.android;

import android.os.AsyncTask;

import com.badlogic.gdx.Gdx;
import com.demodu.turbohearts.api.endpoints.Endpoint;
import com.demodu.turbohearts.api.endpoints.Endpoints;
import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableCreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableLobbyListRequest;
import com.demodu.turbohearts.api.messages.ImmutableRoomRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.api.messages.RoomResponse;
import com.demodu.turbohearts.crossplat.auth.Avatar;
import com.demodu.turbohearts.crossplat.auth.Profile;
import com.demodu.turbohearts.crossplat.lobby.ExampleMatchManager;
import com.demodu.turbohearts.crossplat.lobby.LobbyManager;
import com.demodu.turbohearts.crossplat.lobby.LobbyRoom;
import com.demodu.turbohearts.crossplat.lobby.MatchId;
import com.demodu.turbohearts.crossplat.lobby.RoomOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AndroidLobbyManager implements LobbyManager {

	private int pollingState = 0;
	private AndroidAuthManager androidAuthManager;
	private String userAgent;
	private AndroidLauncher context;
	private Thread pendingLobbyThread;
	private Thread pendingRoomThread;
	private Integer currentRoomId;
	private LobbyRoomListener creator;

	AndroidLobbyManager(AndroidAuthManager androidAuthManager, AndroidLauncher context) {
		this.androidAuthManager = androidAuthManager;
		this.userAgent = context.getString(R.string.user_agent);
		this.context = context;
	}

	@Override
	public synchronized void enterLobby(final Profile profile, final LobbyListener listener) {
		pollingState += 1;
		final int expectedPolling = pollingState;
		if (pendingLobbyThread != null) {
			throw new IllegalStateException("exitLobby must be called before calling enterLobby");
		}
		pendingLobbyThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int latestRevision = -1;
				while (pollingState == expectedPolling) {
					try {
						final LobbyListResponse response =
								Endpoints.lobbyList.send(ImmutableLobbyListRequest
												.builder()
												.authToken(androidAuthManager.getAuthToken())
												.latestRevision(latestRevision)
												.build()
										, userAgent);

						latestRevision = response.getRevision();
						context.postRunnable(new Runnable() {
							@Override
							public void run() {
								if (pollingState == expectedPolling) {
									List<LobbyRoom> lobbyEntryList = new ArrayList<>();
									for (LobbyListResponse.LobbyRoom room : response.getLobbyList()) {

										lobbyEntryList.add(Util.toCore(room));
									}
									listener.onLobbyList(lobbyEntryList);
								}
							}
						});

					} catch (IOException ex) {
						Gdx.app.log("AndroidLobbyManager", "Failed to get lobby list");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex2) {
							Gdx.app.log("AndroidLobbyManager", "Interrupted while retrying");
							break;
						}
					}
				}
			}
		});
		pendingLobbyThread.start();
	}



	@Override
	public synchronized void exitLobby() {
		Gdx.app.log("AndroidLobbyManager", "exit lobby called. isPolling: " + pollingState);
		if (pendingLobbyThread != null) {
			pollingState += 1;
			pendingLobbyThread.interrupt();
			pendingLobbyThread = null;
		}
		Gdx.app.log("AndroidLobbyManager", "Finished exiting");
	}

	@Override
	public void enterRoom(final LobbyRoom entry, final LobbyRoomListener lobbyRoomListener) {
		exitLobby();
		pollRoom(entry.getId(), lobbyRoomListener, true);
	}

	private synchronized void pollRoom(
			final int roomId,
			final LobbyRoomListener lobbyRoomListener,
			final boolean shouldEnterFirst
	) {
		Gdx.app.log("AndroidLobbyManager", "enterRoom called");
		currentRoomId = roomId;
		pollingState += 1;
		final int expectedPollingState = pollingState;
		if (pendingRoomThread != null) {
			throw new IllegalStateException("We're still in a room...");
		}
		pendingRoomThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Gdx.app.log("AndroidLobbyManager", "Enter room polling loop. " + expectedPollingState + " " + pollingState);
				int latestVersion = -1;
				// Main polling loop for room updates
				while (expectedPollingState == pollingState) {
					try {
						Endpoint<RoomRequest, RoomResponse> endpoint;

						if (latestVersion == -1 && shouldEnterFirst) {
							endpoint = Endpoints.Room.enterRoom;
						} else {
							endpoint = Endpoints.Room.pollRoom;
						}


						Gdx.app.log("AndroidLobbyManager", "Polling for room updates");

						final RoomResponse response = endpoint.send(
								ImmutableRoomRequest
										.builder()
										.roomId(roomId)
										.authToken(androidAuthManager.getAuthToken())
										.latestVersion(latestVersion)
										.build(),
								context.getString(R.string.user_agent)
						);


						Gdx.app.log("AndroidLobbyManager",
								"Got response from enter room: " + response.toJsonString());


						context.postRunnable(new Runnable() {
							@Override
							public void run() {
								if (expectedPollingState == pollingState) {
									processRoomResponse(response, lobbyRoomListener);
								}
							}
						});

						if (response.getUpdateType() == RoomResponse.UpdateType.EnteredRoom ||
								response.getUpdateType() == RoomResponse.UpdateType.UpdateRoom) {
							latestVersion = response.getRoom().getVersion();
						} else {
							break;
						}

					} catch (IOException ex) {
						Gdx.app.log("AndroidLobbyManager", "Failed to poll room updates list");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex2) {
							Gdx.app.log("AndroidLobbyManager", "Interrupted while retrying");
							break;
						}
					}
				}

				Gdx.app.log("AndroidLobbyManager", "Exit room polling loop");
			}
		});
		pendingRoomThread.start();
	}

	@Override
	public synchronized void exitRoom() {
		Gdx.app.log("AndroidLobbyManager", "exitRoom called");
		if (pendingRoomThread != null) {
			pollingState += 1;
			pendingRoomThread.interrupt();
			pendingRoomThread = null;

			AsyncTask.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Endpoints.Room.leaveRoom.send(ImmutableRoomRequest
										.builder()
										.authToken(androidAuthManager.getAuthToken())
										.roomId(currentRoomId)
										.build(),
								context.getString(R.string.user_agent)
						);
					} catch (IOException ex) {
						ex.printStackTrace();
						ex.printStackTrace();
						Gdx.app.error("AndroidLobbyManager", "Couldn't leave the room");
					}
				}
			});
		}
	}

	@Override
	public void createRoom(final RoomOptions options, final LobbyRoomListener lobbyRoomListener) {
		creator = lobbyRoomListener;
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				CreateRoomRequest createRoomRequest = ImmutableCreateRoomRequest
						.builder()
						.authToken(androidAuthManager.getAuthToken())
						.name(options.getTitle())
						.build();

				try {
					final RoomResponse response = Endpoints.Room.createRoom.send(
							createRoomRequest,
							context.getString(R.string.user_agent)
					);
					if (response.getRoom() != null) {
						currentRoomId = response.getRoom().getId();
					}
					context.postRunnable(new Runnable() {
						@Override
						public void run() {
						pollRoom(response.getRoom().getId(), lobbyRoomListener, false);
						processRoomResponse(response, lobbyRoomListener);
						}
					});

				} catch (IOException ex) {
					ex.printStackTrace();
					context.postRunnable(new Runnable() {
						@Override
						public void run() {
							lobbyRoomListener.onCancel("Couldn't connect to server");
						}
					});
				}
			}
		});
	}

	private void processRoomResponse(RoomResponse response, LobbyRoomListener lobbyRoomListener) {
		LobbyRoom room = null;
		if (response.getRoom() != null) {
			room = Util.toCore(response.getRoom());
		}
		switch (response.getUpdateType()) {
			case EnteredRoom:
				lobbyRoomListener.onEnter(room);
				break;
			case UpdateRoom:
				assert room != null;
				lobbyRoomListener.onPlayerListUpdate(room.getPlayers());
				break;
			case StartGame:
				exitRoom();
				assert room != null;
				assert response.getGameId() != null;
				lobbyRoomListener.onPlay(
						new AndroidGameConductor(context, androidAuthManager, response.getGameId()),
						new ExampleMatchManager(),
						new MatchId(response.getGameId()),
						new Avatar(response.getGamePlayers().get(1)),
						new Avatar(response.getGamePlayers().get(2)),
						new Avatar(response.getGamePlayers().get(3))
				);
				break;
			case LeaveRoom:
				exitRoom();
				lobbyRoomListener.onCancel(response.getLeaveMessage());
				break;
		}
	}

	@Override
	public synchronized void startGame() {
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final RoomResponse response;
					response = Endpoints.Room.startGame.send(ImmutableRoomRequest
							.builder()
							.roomId(currentRoomId)
							.authToken(androidAuthManager.getAuthToken())
							.build(),
							context.getString(R.string.user_agent)
					);

					Gdx.app.log("AndroidLobbyManager", "Got response from start game: " + response.toJsonString());
				} catch (IOException e) {
					Gdx.app.log("AndroidLobbyManager", "Could not start game");
				}
			}
		});
	}
}
