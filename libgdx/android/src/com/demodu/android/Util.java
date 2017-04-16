package com.demodu.android;

import com.demodu.turbohearts.api.messages.LobbyListResponse;
import com.demodu.turbohearts.crossplat.auth.Avatar;
import com.demodu.turbohearts.crossplat.lobby.LobbyRoom;

import java.util.ArrayList;
import java.util.List;

public class Util extends com.demodu.turbohearts.api.messages.Util{
	public static LobbyRoom toCore(LobbyListResponse.LobbyRoom api) {
		List<Avatar> players = new ArrayList<>();
		for (String p : api.getPlayerNames()) {
			players.add(new Avatar(p));
		}
		return new LobbyRoom(api.getId(), api.getTitle(), players, new Avatar(api.getHost()));
	}
}
