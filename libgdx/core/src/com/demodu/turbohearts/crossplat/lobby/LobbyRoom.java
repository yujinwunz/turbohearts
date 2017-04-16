package com.demodu.turbohearts.crossplat.lobby;

import com.demodu.turbohearts.crossplat.auth.Avatar;

import java.util.ArrayList;
import java.util.List;


public class LobbyRoom {
	private List<Avatar> players;
	private String name;
	private Avatar owner;
	private int id;

	public LobbyRoom(int id, String name, List<Avatar> players, Avatar owner) {
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

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
