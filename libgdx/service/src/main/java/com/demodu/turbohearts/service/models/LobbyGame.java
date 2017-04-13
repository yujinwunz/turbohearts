package com.demodu.turbohearts.service.models;

import org.hibernate.annotations.GenericGenerator;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name=LobbyGame.TABLE)
public class LobbyGame {
	public static final String TABLE = "Lobby";

	private int id;
	private String name;
	private Set<User> players;

	public LobbyGame() {
		// Just for you, Hibernate <3
	}

	public LobbyGame(String name, Set<User> players) {
		this.name = name;
		this.players = players;
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
}
