package com.demodu.android;

import com.demodu.crossplat.auth.AuthManager;
import com.demodu.crossplat.auth.Profile;

/**
 * Created by yujinwunz on 28/03/2017.
 */

public class AndroidAuthManager implements AuthManager {
	@Override
	public void startLogin(LoginMethod method, LoginCallback callback) {
		
	}

	@Override
	public Profile getCurrentLogin() {
		return null;
	}

	@Override
	public void setUsername(String username, LoginCallback callback) {

	}

	@Override
	public void updateDisplayName(String displayName, LoginCallback callback) {

	}
}
