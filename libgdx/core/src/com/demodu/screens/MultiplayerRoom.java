package com.demodu.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.GameContext;
import com.demodu.turboheartsgame.TurboHeartsGame;
import com.demodu.assets.AssetFactory;
import com.demodu.assets.Assets;
import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.crossplat.lobby.LobbyEntry;
import com.demodu.crossplat.lobby.LobbyManager;
import com.demodu.crossplat.lobby.LobbyRoom;
import com.demodu.crossplat.lobby.MatchId;
import com.demodu.crossplat.lobby.MatchManager;
import com.demodu.crossplat.lobby.RoomOptions;
import com.demodu.gamelogic.GameConductor;
import com.demodu.gwtcompat.Callable;

import java.util.List;

/**
 * Created by yujinwunz on 26/03/2017.
 */

public class MultiplayerRoom extends ScreenAdapter {

	private Stage stage = new Stage(new StretchViewport(800, 480));
	private boolean hosting;
	private Cell[] playerCells = new Cell[4];
	private Label titleLabel;
	private GameContext gameContext;
	private LobbyManager lobbyManager;
	private Button startButton;

	// Joined game
	public MultiplayerRoom(
			final GameContext gameContext,
			LobbyManager lobbyManager,
			Profile profile,
			final Callable onLeave,
			LobbyEntry lobbyEntry
	) {
		hosting = false;
		this.gameContext = gameContext;
		this.lobbyManager = lobbyManager;
		makeTable(gameContext, onLeave);

		lobbyManager.enterRoom(lobbyEntry, makeLobbyRoomListener(gameContext, onLeave));
	}

	// Create game
	public MultiplayerRoom(
			final GameContext gameContext,
			LobbyManager lobbyManager,
			Profile profile,
			RoomOptions roomOptions,
			final Callable onLeave
	) {
		hosting = true;
		this.gameContext = gameContext;
		this.lobbyManager = lobbyManager;
		makeTable(gameContext, onLeave);

		lobbyManager.createRoom(roomOptions, makeLobbyRoomListener(gameContext, onLeave));
	}

	private LobbyManager.LobbyRoomListener makeLobbyRoomListener(final GameContext gameContext, final Callable onLeave) {
		return new LobbyManager.LobbyRoomListener() {
			@Override
			public void onEnter(LobbyRoom lobbyRoom) {
				enterRoom(lobbyRoom);
			}

			@Override
			public void onPlayerListUpdate(List<Avatar> players) {
				updatePlayers(players);
			}

			@Override
			public void onPlay(GameConductor gameConductor, MatchManager manager, MatchId matchId, Avatar left, Avatar across, Avatar right) {
				// TODO: Integrate avatars and in game comm.
				gameContext.setScreen(new TurboHeartsGame(gameContext, gameConductor, new Callable() {
					@Override
					public Object call() {
						onLeave.call();
						return null;
					}
				}, left, across, right));
			}

			@Override
			public void onCancel(String message) {
				MultiplayerRoom.this.onCancel(message, onLeave);
			}
		};
	}

	private void onCancel(String message, final Callable onLeave) {
		Gdx.app.log("MultiplayerRoom", "Cancelled");
		gameContext.setScreen(new Menu(message, gameContext, new Menu.MenuItem("Ok", new Callable() {
			@Override
			public Object call() {
				onLeave.call();
				return null;
			}
		})));
	}

	private void enterRoom(LobbyRoom lobbyRoom) {
		Gdx.app.log("MultiplayerRoom", "Room entered");
		titleLabel.setText(lobbyRoom.getName());
		updatePlayers(lobbyRoom.getPlayers());
	}

	private void updatePlayers(List<Avatar> players) {
		Gdx.app.log("MultiplayerRoom", "Players updated");
		for (Cell c : playerCells) {
			c.clearActor();
		}
		for (int i = 0; i < players.size(); i++) {
			playerCells[i].setActor(new Label(
					players.get(i).getDisplayName(),
					AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0))
			);
		}
		if (hosting) {
			startButton.setDisabled(!(players.size() == 4));
		}
	}

	private void makeTable(GameContext gameContext, final Callable onLeave) {
		Table table = new Table();
		table.setFillParent(true);
		table.center();

		Label.LabelStyle labelStyle = AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0);
		titleLabel = new Label("Loading...", labelStyle);
		table.add(titleLabel).colspan(2);
		table.row().pad(50);
		Label playersTitle = new Label("Players:", labelStyle);
		table.add(playersTitle).colspan(2);
		table.row();
		for (int i = 0; i < 4; i++) {
			playerCells[i] = table.add().colspan(2);
			table.row();
		}

		Button leaveButton = new TextButton("Leave", AssetFactory.makeSmallTextButtonStyle(
				gameContext.getManager(),
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B
		));
		leaveButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				lobbyManager.exitRoom();
				onLeave.call();
			}
		});
		table.add(leaveButton).left().bottom();

		if (hosting) {
			startButton = new TextButton("Start Game", AssetFactory.makeSmallTextButtonStyle(
					gameContext.getManager(),
					Assets.Colors.BACKGROUND_COLOUR_R,
					Assets.Colors.BACKGROUND_COLOUR_G,
					Assets.Colors.BACKGROUND_COLOUR_B
			));
			startButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					lobbyManager.startGame();
				}
			});
			startButton.setDisabled(true);
			table.add(startButton).right().bottom();
		}
		stage.addActor(table);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B,
				1
		);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act();
		stage.draw();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}
}
