package com.demodu.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.demodu.crossplat.auth.AuthManager;
import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.gwtcompat.Callable;
import com.demodu.turbohearts.api.endpoints.Endpoints;
import com.demodu.turbohearts.api.messages.ImmutableLoginRequest;
import com.demodu.turbohearts.api.messages.LoginRequest;
import com.demodu.turbohearts.api.messages.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import java.io.IOException;

public class AndroidAuthManager implements AuthManager {

	private AndroidLauncher context;
	private LoginCallback loginCallback;
	private Profile profile;

	public AndroidAuthManager(AndroidLauncher context) {
		this.context = context;
		AndroidLauncher.reporter = new LoginResultReporter() {
			@Override
			public void report(GoogleSignInResult result, Callable onFinish) {
				reportLoginResult(result, onFinish);
			}
		};
	}

	@Override
	public void startLogin(LoginMethod method, LoginCallback callback) {
		this.loginCallback = callback;
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
		return profile;
	}

	@Override
	public void setUsername(String username, LoginCallback callback) {

	}

	@Override
	public void updateDisplayName(String displayName, LoginCallback callback) {

	}

	private void signInWithGoogle() {
		Log.d("AndroidAuthManager", "Signing in with Google. loginCallback: " + this.loginCallback);
		Intent loginIntent = new Intent(context, LoginActivity.class);
		context.startActivity(loginIntent);
	}

	private void fetchAndReportProfile(final GoogleSignInAccount account, final Callable onFinish) {
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				LoginRequest request = ImmutableLoginRequest
						.builder()
						.idToken(account.getIdToken())
						.build();
				try {
					Log.d("AndroidAuthManager", "sending out response");
					final LoginResponse response = Endpoints.loginEndpoint.send(
							request,
							context.getString(R.string.user_agent)
					);

					try {
						Log.d("AndroidAuthManager", "Got response: " + response.toJsonString());
					} catch (JsonProcessingException e) {
						Log.d("AndroidAuthManager", "Couldn't print out JSON of response object");
					}
					if (response.getSuccess()) {
						profile = new Profile(
								new Avatar(response.displayName()),
								response.getUsername()
						);
						context.postRunnable(new Runnable() {
							@Override
							public void run() {
								loginCallback.onSuccess(profile);
							}
						});
					} else {
						context.postRunnable(new Runnable() {
							@Override
							public void run() {
								loginCallback.onFailure("Could not log in: " + response.getErrorMessage());
							}
						});
					}

				} catch (IOException e) {

					context.postRunnable(new Runnable() {
						@Override
						public void run() {
							loginCallback.onFailure("Could not connect to the login server");
						}
					});
					Gdx.app.log("AndroidAuthManager", "could not connect to the login server");
				} finally {
					onFinish.call();
				}
			}
		});
	}

	protected void reportLoginResult(final GoogleSignInResult result, Callable onFinish) {
		if (result.isSuccess()) {
			fetchAndReportProfile(result.getSignInAccount(), onFinish);
		} else {
			context.postRunnable(new Runnable() {
				@Override
				public void run() {
					Log.d("AndroidAuthManager", "reporting Login result. Callback: " + loginCallback);
					loginCallback.onFailure("GoogleSignInResult: " + result.getStatus().getStatusCode());
				}
			});
			onFinish.call();
		}
	}
}
