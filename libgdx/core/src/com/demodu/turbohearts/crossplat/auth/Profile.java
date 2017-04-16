package com.demodu.turbohearts.crossplat.auth;


public class Profile {
	private Avatar avatar;
	// A null username indicates a half-registered user.
	private String username;

	public Avatar getAvatar() {
		return avatar;
	}

	public String getUsername() {
		return username;
	}

	public Profile(Avatar avatar, String username) {
		this.avatar = avatar;
		this.username = username;

	}
}
