package com.demodu.crossplat.lobby;

import com.demodu.crossplat.auth.Avatar;

import java.util.List;

public class LobbyEntry {
	private MatchId id;
	private String name;
	private List<Avatar> players;

	public LobbyEntry(MatchId id, String name, List<Avatar> players) {
		this.id = id;
		this.name = name;
		this.players = players;
	}

	public MatchId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Avatar> getPlayers() {
		return players;
	}
}
