package com.demodu.screens;

import com.demodu.GameContext;
import com.demodu.gwtcompat.Callable;

/**
 * Created by yujinwunz on 27/03/2017.
 */

public class LoginSelection extends Menu {
	public LoginSelection(
			GameContext gameContext,
			Callable onFacebook,
			Callable onGoogle,
			Callable onUsername
	) {
		super(
				"Login or register to continue",
				gameContext,
				new MenuItem("Login with Facebook", onFacebook),
				new MenuItem("Login with Google", onGoogle),
				new MenuItem("Login with Username", onUsername)
		);
	}
}
