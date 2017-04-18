package com.demodu.turbohearts.screens;

import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.gwtcompat.Callable;

public class MainMenu extends Menu {
	public MainMenu(
			GameContext gameContext,
			Callable onBack,
			Callable singlePlayer,
			Callable multiPlayer,
			Callable leaderBoards
	) {
		super(
				"TurboHearts!",
				gameContext,
				onBack,
				Menu.createMenuItem("Single player", singlePlayer),
				Menu.createMenuItem("Multi player", multiPlayer),
				Menu.createMenuItem("Rankings (lies)", leaderBoards)
		);
	}
}
