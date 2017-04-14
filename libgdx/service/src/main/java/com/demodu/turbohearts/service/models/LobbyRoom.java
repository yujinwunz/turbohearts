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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name= LobbyRoom.TABLE)
public class LobbyRoom {
	public static final String TABLE = "Lobby";

	private int id;
	private int version;
	private String name;
	private Set<User> players;
	private User host;

	public LobbyRoom() {
		// Just for you, Hibernate <3
	}

	public LobbyRoom(String name, Set<User> players, int version, User host) {
		this.name = name;
		this.players = players;
		this.version = version;
		this.host = host;
	}

	@ManyToOne
	public User getHost() {
		return host;
	}

	public void setHost(User host) {
		this.host = host;
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

	@ManyToMany
	public Set<User> getPlayers() {
		return players;
	}

	@PreUpdate
	public void versionCheck() {

	}

	public synchronized void setPlayers(Set<User> players) {
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
				.host(getHost().getUsername())
				.version(getVersion())
				.build();
		return item;
	}

	public synchronized boolean hasPlayer(User user) {
		for (User u : players) {
			if (u.getId().equals(user.getId())) {
				return true;
			}
		}
		return false;
	}

	public synchronized void removePlayer(User user) {
		players.remove(user);
	}
}
