package com.demodu.turbohearts.screens;

import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.gwtcompat.Callable;

/**
 * Created by yujinwunz on 27/03/2017.
 */

public class LoginSelection extends Menu {
	public LoginSelection(
			GameContext gameContext,
			Callable onBack,
			Callable onFacebook,
			Callable onGoogle,
			Callable onUsername
	) {
		super(
				"Login or register to continue",
				gameContext,
				onBack,
				new MenuItem("Login with Facebook (lies)", onFacebook),
				new MenuItem("Login with Google", onGoogle),
				new MenuItem("Login with Username (lies)", onUsername)
		);
	}
}
