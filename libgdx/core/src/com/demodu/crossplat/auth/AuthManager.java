package com.demodu.crossplat.auth;

/**
 * Created by yujinwunz on 25/03/2017.
 */

public interface AuthManager {
	void startLogin(LoginMethod method, LoginCallback callback);
	Profile getCurrentLogin();

	interface LoginCallback {
		void onFailure(String message);
		void onSuccess(Profile profile);
	}

	enum LoginMethod {
		Facebook, Google, Username
	}
}
