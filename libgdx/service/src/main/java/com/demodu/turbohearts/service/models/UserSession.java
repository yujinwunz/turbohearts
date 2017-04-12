package com.demodu.turbohearts.service.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yujinwunz on 10/04/2017.
 */

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
	@Column(name="idToken")
	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	@Column(name="user")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
