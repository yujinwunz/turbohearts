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
					currentProfile = new Profile(new Avatar("Test User"), null);
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

	@Override
	public void register(final String username, String displayName, final LoginCallback callback) {
		if (currentProfile == null) {
			throw new NullPointerException("Not logged in");
		} else if (currentProfile.getUsername() != null) {
			throw new UnsupportedOperationException("Cannot change existing username");
		}
		Timer.schedule(new Timer.Task() {
			@Override
			public void run() {
				currentProfile = new Profile(new Avatar(username), username);
				callback.onSuccess(currentProfile);
			}
		}, 2.0f);
	}

	@Override
	public void updateDisplayName(final String displayName, final LoginCallback callback) {
		if (currentProfile == null) {
			throw new NullPointerException("Not logged in");
		} else if (currentProfile.getUsername() != null) {
			throw new UnsupportedOperationException("Cannot change existing username");
		}
		Timer.schedule(new Timer.Task() {
			@Override
			public void run() {
				currentProfile = new Profile(new Avatar(displayName), currentProfile.getUsername());
				callback.onSuccess(currentProfile);
			}
		}, 2.0f);
	}
}
