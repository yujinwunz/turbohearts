package com.demodu.crossplat.lobby;

import com.demodu.crossplat.auth.Avatar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yujinwunz on 26/03/2017.
 */

public class LobbyRoom {
	private List<Avatar> players;
	private String name;
	private Avatar owner;
	private MatchId id;

	public LobbyRoom(MatchId id, String name, List<Avatar> players, Avatar owner) {
		this.id = id;
		this.name = name;
		this.players = players;
		this.owner = owner;
	}

	public List<Avatar> getPlayers() {
		return new ArrayList<Avatar>(players);
	}

	public Avatar getOwner() {
		return owner;
	}

	public MatchId getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
