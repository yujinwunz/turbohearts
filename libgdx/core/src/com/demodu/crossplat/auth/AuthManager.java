package com.demodu.crossplat.auth;

/**
 * Created by yujinwunz on 25/03/2017.
 */

public interface AuthManager {
	void startLogin(LoginMethod method, LoginCallback callback);
	Profile getCurrentLogin();
	void setUsername(String username, LoginCallback callback);
	void updateDisplayName(String displayName, LoginCallback callback);

	interface LoginCallback {
		void onFailure(String message);
		void onSuccess(Profile profile);
	}

	enum LoginMethod {
		Facebook, Google, Username
	}
}
