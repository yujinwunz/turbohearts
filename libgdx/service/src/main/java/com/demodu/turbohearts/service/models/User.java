package com.demodu.turbohearts.service.models;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=User.TABLE)
public class User {
	public static final String TABLE = "Users";

	private String id;
	private String username;
	private String displayName;

	public User() {
		// Used by hibernate
	}

	public User(String id, String username, String displayName) {
		this.id = id;
		this.username = username;
		this.displayName = displayName;
	}

	@Id
	public String getId() {
		return id;
	}

	@Column(name="username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name="display_name")
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof User) {
			return ((User) o).getId().equals(getId());
		} else {
			return false;
		}
	}
}
