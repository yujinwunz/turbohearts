package com.demodu.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.demodu.TurboHearts;
import com.demodu.crossplat.content.ExampleContentManager;
import com.demodu.crossplat.lobby.ExampleLobbyManager;

import java.util.Arrays;

public class AndroidLauncher extends AndroidApplication {

	public static LoginResultReporter reporter;

	public AndroidLauncher() {
		super();
		Log.d("AndroidLauncher", "Constructed");
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		AndroidAuthManager androidAuthManager = new AndroidAuthManager(this);
		initialize(new TurboHearts(
				androidAuthManager,
				new ExampleContentManager(),
				new AndroidLobbyManager()
		), config);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Gdx.app.log("AndroidApplication", "onStop called");
	}

	@Override
	protected void onDestroy() {
		Gdx.app.log(
				"AndroidLauncher",
				"onDestroy called from " +
						Arrays.toString(Thread.currentThread().getStackTrace())
		);
		super.onDestroy();
		Gdx.app.log("AndroidApplication", "onDestroy called");
	}
}
