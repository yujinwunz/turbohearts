package com.demodu.crossplat.lobby;

import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.gamelogic.GameConductor;

import java.util.List;

public interface LobbyManager {
	void enterLobby(Profile profile, LobbyListener listener);
	// Halts updates about lobby games.
	void exitLobby();

	void enterRoom(LobbyEntry entry, LobbyRoomListener lobbyRoomListener);
	// Tells server that we've left and halts updates.
	void exitRoom();

	// creates a room and enters it.
	void createRoom(LobbyRoomListener lobbyRoomListener);

	// Calls the onPlay method of the current lobbyRoomListener if success.
	void startGame(LobbyRoomListener lobbyRoomListener);

	interface LobbyListener {
		void onLobbyList(List<LobbyEntry> lobbyList);
	}

	interface LobbyRoomListener {
		void onEnter(LobbyRoom lobbyRoom);
		boolean onPlayerListUpdate(List<Avatar> players);
		void onPlay(
				GameConductor gameConductor,
				MatchManager manager,
				MatchId matchId,
				Avatar left,
				Avatar across,
				Avatar right
		);
		void onCancel(String message);
	}
}
