package com.demodu.android;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.demodu.TurboHearts;
import com.demodu.crossplat.content.ExampleContentManager;
import com.demodu.crossplat.lobby.ExampleLobbyManager;

import java.util.Arrays;

public class AndroidLauncher extends AndroidApplication {

	public static LoginResultReporter reporter;

	protected AndroidAuthManager androidAuthManager = new AndroidAuthManager(this);

	public AndroidLauncher() {
		super();
		Log.d("AndroidLauncher", "Constructed");
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new TurboHearts(
				androidAuthManager,
				new ExampleContentManager(),
				new ExampleLobbyManager()
		), config);
		Gdx.app.log("AndroidApplication", "onCreate called");

		Gdx.app.addLifecycleListener(new LifecycleListener() {
			@Override
			public void pause() {
				Gdx.app.log("App", "Pausing");
			}

			@Override
			public void resume() {
				Gdx.app.log("App", "Resume");
			}

			@Override
			public void dispose() {
				Gdx.app.log("App", "Dispose");
			}
		});

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
