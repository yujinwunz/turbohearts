package com.demodu.turbohearts.service.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = Participation.TABLE)
public class Participation {
	public static final String TABLE = "Participation";

	private int index;
	private User user;

	public Participation() {
		// For hibernate
	}

	public Participation(int index, User user) {
		this.index = index;
		this.user = user;
	}

	@Column(name="index")
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@ManyToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
