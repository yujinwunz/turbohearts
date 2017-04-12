package com.demodu.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.demodu.gwtcompat.Callable;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

	public static final int RC_SIGN_IN = 1001;

	GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Configure sign-in to request the user's ID, email address, and basic
		// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.requestIdToken(getString(R.string.server_client_id))
				.build();

		// Build a GoogleApiClient with access to the Google Sign-In API and the
		// options specified by gso.
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		signIn();
	}

	public void signIn() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		throw new UnknownError("Connection failed");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
	}

	private void handleSignInResult(GoogleSignInResult result) {

		AndroidLauncher.reporter.report(result, new Callable() {
			@Override
			public Object call() {
				finish();
				return null;
			}
		});

		Log.d("LoginActivity", "handleSignInResult:" + result.isSuccess());
		Log.d("LoginActivity", "status:" + result.getStatus());
		if (result.isSuccess()) {
			// Signed in successfully, show authenticated UI.
			GoogleSignInAccount acct = result.getSignInAccount();
			Log.d("LoginActivity", getString(R.string.signed_in_fmt, acct.getDisplayName()));
			Log.d("LoginActivity", "Id:" + acct.getId());
			Log.d("LoginActivity", "Id token:" + acct.getIdToken());
		} else {
			Log.d("LoginActivity", "Signed out");
		}
	}
}
