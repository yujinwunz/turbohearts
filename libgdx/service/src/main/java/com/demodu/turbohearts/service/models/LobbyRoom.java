package com.demodu.turbohearts.service.models;

import com.demodu.turbohearts.api.messages.ImmutableLobbyRoom;
import com.demodu.turbohearts.api.messages.LobbyListResponse;

import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name= LobbyRoom.TABLE)
public class LobbyRoom {
	public static final String TABLE = "Lobby";

	private int id;
	private int version;
	private String name;
	private Set<User> players;

	public LobbyRoom() {
		// Just for you, Hibernate <3
	}

	public LobbyRoom(String name, Set<User> players, int version) {
		this.name = name;
		this.players = players;
		this.version = version;
	}

	@Column(name="version")
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany
	public Set<User> getPlayers() {
		return players;
	}

	public void setPlayers(Set<User> players) {
		this.players = players;
	}

	// Some utility methods
	public LobbyListResponse.LobbyRoom toApi () {
		List<String> players = new ArrayList<>();
		for (User u : getPlayers()) {
			players.add(u.getDisplayName());
		}
		LobbyListResponse.LobbyRoom item = ImmutableLobbyRoom
				.builder()
				.addAllPlayerNames(players)
				.title(getName())
				.id(getId())
				.version(getVersion())
				.build();
		return item;
	}
}