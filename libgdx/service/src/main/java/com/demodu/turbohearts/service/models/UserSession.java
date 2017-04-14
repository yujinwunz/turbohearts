package com.demodu.turbohearts.service.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name= UserSession.TABLE)
public class UserSession {
	public final static String TABLE = "UserSession";

	private String idToken;
	private User user;

	public UserSession() {
		// Used by hibernate
	}

	public UserSession(String idToken, User user) {
		this.idToken = idToken;
		this.user = user;
	}

	@Id
	@Column(name="idToken", length = 2000)
	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	@OneToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
