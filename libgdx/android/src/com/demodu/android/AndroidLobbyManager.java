package com.demodu.android;

import android.os.AsyncTask;

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
	private int latestRevision = -1;

	public AndroidLobbyManager(AndroidAuthManager androidAuthManager, AndroidLauncher context) {
		this.androidAuthManager = androidAuthManager;
		this.userAgent = context.getString(R.string.user_agent);
		this.context = context;
	}

	@Override
	public void enterLobby(final Profile profile, final LobbyListener listener) {
		final int expectedPolling;
		synchronized (this) {
			if (isPolling % 2 == 1) {
				return;
			}
			isPolling += 1;
			expectedPolling = isPolling;
		}
		latestRevision = -1;
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				while (isPolling == expectedPolling) {
					try {
						final LobbyListResponse response =
								Endpoints.lobbyListEndpoint.send(ImmutableLobbyListRequest
										.builder()
										.authToken(androidAuthManager.getAuthToken())
										.latestRevision(latestRevision)
										.build()
								, userAgent);

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
						Gdx.app.debug("AndroidLobbyManager", "Failed to get lobby list");
					}
				}
			}
		});
	}



	@Override
	public void exitLobby() {
		synchronized (this) {
			if (isPolling % 2 == 0) {
				return;
			}
			isPolling += 1;
		}
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
