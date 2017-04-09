package com.demodu.android;

import android.content.Intent;
import android.os.AsyncTask;

import com.demodu.crossplat.auth.AuthManager;
import com.demodu.crossplat.auth.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

/**
 * Created by yujinwunz on 28/03/2017.
 */

public class AndroidAuthManager implements AuthManager {

	private AndroidLauncher context;
	private LoginCallback loginCallback;
	private Profile profile;

	public AndroidAuthManager(AndroidLauncher context) {
		this.context = context;
	}

	@Override
	public void startLogin(LoginMethod method, LoginCallback callback) {
		this.loginCallback = loginCallback;
		switch (method) {
			case Facebook:
				throw new UnsupportedOperationException("Facebook login not supported");
			case Google:
				signInWithGoogle();
				break;
			case Username:
				throw new UnsupportedOperationException("Username login not supported");
		}
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

	private void signInWithGoogle() {
		Intent loginIntent = new Intent(context, LoginActivity.class);
		context.startActivity(loginIntent);
	}

	private void fetchAndReportProfile(GoogleSignInAccount account) {
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				
			}
		});
	}

	protected void reportLoginResult(GoogleSignInResult result) {
		if (result.isSuccess()) {
			fetchAndReportProfile(result.getSignInAccount());
		} else {
			loginCallback.onFailure("Login failure: " + result.getStatus().getStatusMessage());
		}
	}
}
