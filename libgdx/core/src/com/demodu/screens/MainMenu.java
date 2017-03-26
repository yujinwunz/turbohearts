package com.demodu.screens;

import com.demodu.GameContext;
import com.demodu.gwtcompat.Callable;

/**
 * Created by yujinwunz on 26/03/2017.
 */

public class MainMenu extends Menu {
	public MainMenu(
			GameContext gameContext,
			Callable singlePlayer,
			Callable multiPlayer,
			Callable leaderBoards
	) {
		super(
				"TurboHearts!",
				gameContext,
				Menu.createMenuItem("Single player", singlePlayer),
				Menu.createMenuItem("Multi player", multiPlayer),
				Menu.createMenuItem("Leaderboards", leaderBoards)
		);
	}
}
