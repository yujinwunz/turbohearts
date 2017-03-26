package com.demodu.crossplat.auth;

import com.badlogic.gdx.utils.Timer;

import java.util.Random;

public class ExampleAuthManager implements AuthManager {
	private Profile currentProfile = null;
	private Random random = new Random();

	@Override
	public void startLogin(LoginMethod method, final LoginCallback callback) {
		Timer.schedule(new Timer.Task() {
			@Override
			public void run() {
				if (Math.abs(random.nextInt()) % 2 == 1) {
					currentProfile = new Profile(new Avatar("Test User"));
					callback.onSuccess(currentProfile);
				} else {
					callback.onFailure("Randomly failed");
				}
			}
		}, 2.0f);

	}

	@Override
	public Profile getCurrentLogin() {
		return currentProfile;
	}
}
