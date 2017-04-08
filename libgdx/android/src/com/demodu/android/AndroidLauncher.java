package com.demodu.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.demodu.TurboHearts;
import com.demodu.crossplat.content.ExampleContentManager;
import com.demodu.crossplat.lobby.ExampleLobbyManager;

public class AndroidLauncher extends AndroidApplication {
	protected AndroidAuthManager androidAuthManager = new AndroidAuthManager(this);

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new TurboHearts(
				androidAuthManager,
				new ExampleContentManager(),
				new ExampleLobbyManager()
		), config);
	}
}
