package com.demodu.android;

import android.os.AsyncTask;

import com.badlogic.gdx.Gdx;
import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.crossplat.lobby.ExampleMatchManager;
import com.demodu.crossplat.lobby.LobbyEntry;
import com.demodu.crossplat.lobby.LobbyManager;
import com.demodu.crossplat.lobby.LobbyRoom;
import com.demodu.crossplat.lobby.MatchId;
import com.demodu.crossplat.lobby.RoomOptions;
import com.demodu.gamelogic.LocalGameConductor;
import com.demodu.player.RandomAI;
import com.demodu.turbohearts.api.endpoints.Endpoint;
import com.demodu.turbohearts.api.endpoints.Endpoints;
import com.demodu.turbohearts.api.messages.CreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableCreateRoomRequest;
import com.demodu.turbohearts.api.messages.ImmutableLobbyListRequest;
import com.demodu.turbohearts.api.messages.ImmutableRoomRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.api.messages.RoomRequest;
import com.demodu.turbohearts.api.messages.RoomResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndroidLobbyManager implements LobbyManager {

	private int pollingState = 0;
	private AndroidAuthManager androidAuthManager;
	private String userAgent;
	private AndroidLauncher context;
	private Thread pendingLobbyThread;
	private Thread pendingRoomThread;

	public AndroidLobbyManager(AndroidAuthManager androidAuthManager, AndroidLauncher context) {
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
								Endpoints.lobbyListEndpoint.send(ImmutableLobbyListRequest
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
									List<LobbyEntry> lobbyEntryList = new ArrayList<>();
									for (LobbyListResponse.LobbyRoom room : response.getLobbyList()) {
										List<Avatar> playerList = new ArrayList<Avatar>();
										for (String name : room.getPlayerNames()) {
											playerList.add(new Avatar(name));
										}
										lobbyEntryList.add(new LobbyEntry(
												Integer.toString(room.getId()),
												room.getTitle(),
												playerList)
										);
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
							throw new UnknownError("Error while cooling down between retries");
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
		pollingState += 1;
		pendingLobbyThread.interrupt();
		pendingLobbyThread = null;
		Gdx.app.log("AndroidLobbyManager", "Finished exiting");
	}

	@Override
	public synchronized void enterRoom(final LobbyEntry entry, final LobbyRoomListener lobbyRoomListener) {
		pollingState += 1;
		final int expectedPollingState = pollingState;
		if (pendingRoomThread != null) {
			throw new IllegalStateException("We're still in a room...");
		}
		pendingRoomThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int latestVersion = -1;
				while (true) {
					try {
						Endpoint<RoomRequest, RoomResponse> endpoint;
						if (latestVersion == -1) {
							endpoint = Endpoints.Room.enterRoomEndpoint;
						} else {
							endpoint = Endpoints.Room.pollRoomEndpoint;
						}

						final RoomResponse response = endpoint.send(
								ImmutableRoomRequest
										.builder()
										.roomId(Integer.parseInt(entry.getId()))
										.authToken(androidAuthManager.getAuthToken())
										.latestVersion(latestVersion)
										.build(),
								context.getString(R.string.user_agent)
						);

						Gdx.app.log("AndroidLobbyManager",
								"Got response from enter room: " + response.toJsonString());

						if (response.getUpdateType() == RoomResponse.UpdateType.EnteredRoom ||
								response.getUpdateType() == RoomResponse.UpdateType.UpdateRoom) {
							latestVersion = response.getRoom().getVersion();
						}

						context.postRunnable(new Runnable() {
							@Override
							public void run() {
								processRoomResponse(response, lobbyRoomListener);
							}
						});

					} catch (IOException ex) {
						Gdx.app.log("AndroidLobbyManager", "Failed to poll room updates list");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex2) {
							throw new UnknownError("Error while cooling down between retries");
						}
					}
				}
			}
		});
		pendingRoomThread.start();
	}

	@Override
	public void exitRoom() {
		pollingState += 1;
		pendingRoomThread.interrupt();
		pendingRoomThread = null;
	}

	@Override
	public void createRoom(final RoomOptions options, final LobbyRoomListener lobbyRoomListener) {
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				CreateRoomRequest createRoomRequest = ImmutableCreateRoomRequest
						.builder()
						.authToken(androidAuthManager.getAuthToken())
						.name(options.getTitle())
						.build();

				try {
					final RoomResponse response = Endpoints.Room.createRoomEndpoint.send(createRoomRequest, context.getString(R.string.user_agent));
					context.postRunnable(new Runnable() {
						@Override
						public void run() {
							if (response != null) {
								processRoomResponse(response, lobbyRoomListener);
							} else {
								lobbyRoomListener.onCancel("Couldn't create new room");
							}
						}
					});

				} catch (IOException ex) {
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
			List<Avatar> avatarList = new ArrayList<>();
			for (String name : response.getRoom().getPlayerNames()) {
				avatarList.add(new Avatar(name));
			}
			room = new LobbyRoom(
					Integer.toString(response.getRoom().getId()),
					response.getRoom().getTitle(),
					avatarList,
					avatarList.get(0)
			);
		}
		switch (response.getUpdateType()) {
			case EnteredRoom:
				lobbyRoomListener.onEnter(room);
				break;
			case UpdateRoom:
				lobbyRoomListener.onPlayerListUpdate(room.getPlayers());
				break;
			case StartGame:
				lobbyRoomListener.onPlay(
						new LocalGameConductor(
								new RandomAI(),
								new RandomAI(),
								new RandomAI()),
						new ExampleMatchManager(),
						new MatchId(response.getGameId()),
						room.getPlayers().get(1),
						room.getPlayers().get(2),
						room.getPlayers().get(3)
				);
				break;
			case LeaveRoom:
				exitRoom();
				lobbyRoomListener.onCancel(response.getLeaveMessage());
				break;
		}
	}

	@Override
	public void startGame() {
		
	}
}
