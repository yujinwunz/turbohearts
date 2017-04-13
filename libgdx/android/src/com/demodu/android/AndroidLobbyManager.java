package com.demodu.android;

import com.badlogic.gdx.Gdx;
import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.crossplat.lobby.LobbyEntry;
import com.demodu.crossplat.lobby.LobbyManager;
import com.demodu.crossplat.lobby.RoomOptions;
import com.demodu.turbohearts.api.endpoints.Endpoints;
import com.demodu.turbohearts.api.messages.ImmutableLobbyListRequest;
import com.demodu.turbohearts.api.messages.LobbyListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndroidLobbyManager implements LobbyManager {

	private int isPolling = 0;
	private AndroidAuthManager androidAuthManager;
	private String userAgent;
	private AndroidLauncher context;
	private Thread pendingThread;

	public AndroidLobbyManager(AndroidAuthManager androidAuthManager, AndroidLauncher context) {
		this.androidAuthManager = androidAuthManager;
		this.userAgent = context.getString(R.string.user_agent);
		this.context = context;
	}

	@Override
	public synchronized void enterLobby(final Profile profile, final LobbyListener listener) {
		Gdx.app.log("AndroidLobbyManager", "enterLobby called. isPolling: " + isPolling);
		final int expectedPolling;
		if (isPolling % 2 == 1) {
			return;
		}
		isPolling += 1;
		expectedPolling = isPolling;
		if (pendingThread != null) {
			throw new IllegalStateException("exitLobby must be called before calling enterLobby");
		}
		pendingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Gdx.app.log("AndroidLobbyManager", "entering lobby");
				int latestRevision = -1;
				while (isPolling == expectedPolling) {
					try {
						Gdx.app.log("AndroidLobbyManager", "Polling server");
						final LobbyListResponse response =
								Endpoints.lobbyListEndpoint.send(ImmutableLobbyListRequest
												.builder()
												.authToken(androidAuthManager.getAuthToken())
												.latestRevision(latestRevision)
												.build()
										, userAgent);

						Gdx.app.log("AndroidLobbyManager", "Got response " + response.toJsonString());

						if (isPolling == expectedPolling) {
							latestRevision = response.getRevision();
							context.postRunnable(new Runnable() {
								@Override
								public void run() {
									if (isPolling == expectedPolling) {
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
						}

					} catch (IOException ex) {
						Gdx.app.log("AndroidLobbyManager", "Failed to get lobby list");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex2) {
							throw new UnknownError("Error while cooling down between retries");
						}
					}
				}
				Gdx.app.log("AndroidLobbyManager", "polling thread exit. isPolling: " + isPolling + " expectedPolling: " + expectedPolling);
			}
		});
		pendingThread.start();
		Gdx.app.log("AndroidLobbyManager", "Finished entering lobby");
	}



	@Override
	public synchronized void exitLobby() {
		Gdx.app.log("AndroidLobbyManager", "exit lobby called. isPolling: " + isPolling);
		if (isPolling % 2 == 0) {
			return;
		}
		isPolling += 1;
		pendingThread.interrupt();
		pendingThread = null;
		Gdx.app.log("AndroidLobbyManager", "Finished exiting");
	}

	@Override
	public void enterRoom(LobbyEntry entry, LobbyRoomListener lobbyRoomListener) {
		
	}

	@Override
	public void exitRoom() {

	}

	@Override
	public void createRoom(RoomOptions options, LobbyRoomListener lobbyRoomListener) {

	}

	@Override
	public void startGame() {

	}
}
