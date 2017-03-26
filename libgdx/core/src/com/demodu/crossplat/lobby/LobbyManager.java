package com.demodu.crossplat.lobby;

import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.gamelogic.GameConductor;

import java.util.List;

public interface LobbyManager {
	void enterLobby(Profile profile, LobbyListener listener);
	void exitLobby();

	void enterRoom(LobbyEntry entry, LobbyRoomListener lobbyRoomListener);
	void exitRoom();

	// Calls the onPlay method of the current lobbyRoomListener if success.
	void startGame();

	interface LobbyListener {
		void onLobbyList(List<LobbyEntry> lobbyList);
	}

	interface LobbyRoomListener {
		void onEnter(LobbyRoom lobbyRoom);
		boolean onPlayerListUpdate(List<Avatar> players);
		void onPlay(
				GameConductor gameConductor,
				MatchId matchId,
				MatchManager manager,
				Avatar left,
				Avatar across,
				Avatar right
		);
	}
}
