package com.demodu.turbohearts.service.models;

import org.hibernate.annotations.GenericGenerator;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name=Game.TABLE)
public class Game {
	public static final String TABLE = "Game";

	private int id;
	private List<Participation> participants;

	public Game() {
		// For hibernate
	}

	public Game(List<Participation> participants) {
		this.participants = participants;
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

	@ManyToMany
	@OrderBy("index")
	public List<Participation> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participation> participants) {
		this.participants = participants;
	}
}
