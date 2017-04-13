package com.demodu.crossplat.lobby;

import com.demodu.crossplat.auth.Avatar;

import java.util.List;

public class LobbyEntry {
	private String id;
	private String name;
	private List<Avatar> players;

	public LobbyEntry(String id, String name, List<Avatar> players) {
		this.id = id;
		this.name = name;
		this.players = players;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Avatar> getPlayers() {
		return players;
	}
}
