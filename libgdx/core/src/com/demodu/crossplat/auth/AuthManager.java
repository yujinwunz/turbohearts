package com.demodu.crossplat.auth;

/**
 * Created by yujinwunz on 25/03/2017.
 */

public interface AuthManager {
	void triggerLogin(LoginMethod method);
	void registerCallback(LoginCallback callback);


	interface LoginCallback {
		void onFailure(String message);
		void onSuccess(Profile profile);
	}

	enum LoginMethod {
		Facebook, Google, Username
	}
}
