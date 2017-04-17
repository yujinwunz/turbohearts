package com.demodu.turbohearts.service.models;

import com.demodu.turbohearts.service.game.LiveGame;

import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

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

	@ManyToMany(targetEntity = Participation.class, cascade = CascadeType.ALL)
	@OrderBy("index")
	public List<Participation> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participation> participants) {
		this.participants = participants;
	}

	@Transient
	public List<String> getNameListFromPerspective(User u)
			throws LiveGame.UserNotInGameException {
		if (participants.size() != 4) {
			throw new IllegalStateException("There must be four participants to create a valid perspective.");
		}

		for (int i = 0; i < 4; i++) {
			if (participants.get(i).getUser().equals(u)) {
				List<String> names = new ArrayList<>();
				for (int j = 0; j < 4; j++) {
					names.add(participants.get((i+j)%4).getUser().getDisplayName());
				}
				return names;
			}
		}
		throw new LiveGame.UserNotInGameException("Could not create perspective for user " + u.getUsername());
	}

	@Transient
	public List<String> getNameList() {
		List<String> names = new ArrayList<>();
		for (Participation p: participants) {
			names.add(p.getUser().getDisplayName());
		}
		return names;
	}
}
