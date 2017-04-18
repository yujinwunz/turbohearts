package com.demodu.turbohearts.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.assets.AssetFactory;
import com.demodu.turbohearts.assets.Assets;
import com.demodu.turbohearts.crossplat.auth.Avatar;
import com.demodu.turbohearts.crossplat.auth.Profile;
import com.demodu.turbohearts.crossplat.lobby.LobbyManager;
import com.demodu.turbohearts.crossplat.lobby.LobbyRoom;
import com.demodu.turbohearts.crossplat.lobby.RoomOptions;
import com.demodu.turbohearts.gwtcompat.Callable;

import java.util.List;

public class MultiplayerLobby extends TurboScreen {

	private LobbyManager lobbyManager;
	private Profile profile;
	private LobbyManager.LobbyListener lobbyListener = new LobbyManager.LobbyListener() {
		@Override
		public void onLobbyList(List<LobbyRoom> lobbyList) {
			Gdx.app.log("MultiplayerLobby", "Lobby list update received with " + lobbyList.size() + " items");
			updateLobbyList(lobbyList);
		}
	};

	private Stage stage = new Stage(new StretchViewport(800, 480));
	private Cell centerCell;
	private GameContext gameContext;
	private TextButton.TextButtonStyle buttonStyle;

	private Callable onBack;

	public MultiplayerLobby(
			GameContext gameContext,
			Profile profile,
			final LobbyManager lobbyManager,
			final Callable onBack
	) {
		this.lobbyManager = lobbyManager;
		this.profile = profile;
		this.gameContext = gameContext;
		this.onBack = onBack;

		Table table = new Table();
		table.setFillParent(true);
		centerCell = table.add(makeLoadingWidget()).expand().colspan(3);
		table.row();
		this.buttonStyle = AssetFactory.makeSmallTextButtonStyle(
				gameContext.getManager(),
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B
		);
		Button back = new TextButton(
				"Back to main",
				buttonStyle
		);
		back.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				onBack.call();
			}
		});
		table.add(back).left().bottom();

		Skin skin = gameContext.getManager().get(Assets.UI_SKIN);
		final TextField nameTextField = new TextField(profile.getUsername() + "'s Game", skin);
		table.add(nameTextField).right().bottom().width(300).expandX().pad(10);

		Button newGame = new TextButton(
				"Create +",
				buttonStyle
		);
		newGame.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				createRoom(nameTextField.getText());
			}
		});
		table.add(newGame).right().bottom();

		stage.addActor(table);
		gameContext.setInputProcessor(stage);
	}

	private Actor makeLoadingWidget() {
		return new Label("Loading...", AssetFactory.makeSmallLabelStyle(
				gameContext.getManager(),
				1, 1, 0
		));
	}

	private void updateLobbyList(List<LobbyRoom> lobbyList) {
		Table lobbyTable = new Table();
		lobbyTable.top();

		for (final LobbyRoom lobbyEntry: lobbyList) {
			Label title = new Label(lobbyEntry.getName(),
					AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0)
			);
			title.setWrap(true);
			lobbyTable.add(title).width(500);
			String playerList = "";
			for (Avatar a : lobbyEntry.getPlayers()) {
				if (playerList.length() > 0) {
					playerList += ", ";
				}
				playerList += a.getDisplayName();
			}
			Label playerListLabel = new Label(playerList,
					AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0)
			);
			playerListLabel.setWrap(true);
			lobbyTable.add(playerListLabel).width(200);

			Button joinButton = new TextButton("Join", buttonStyle);
			if (lobbyEntry.getPlayers().size() == 4) {
				joinButton.setDisabled(true);
			} else {
				joinButton.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						enterRoom(lobbyEntry);
					}
				});
			}
			lobbyTable.add(joinButton).width(100);
			lobbyTable.row();
		}

		ScrollPane scrollPane = new ScrollPane(lobbyTable);

		centerCell.setActor(scrollPane);
	}

	private void enterRoom(LobbyRoom lobbyEntry) {
		gameContext.setScreen(new MultiplayerRoom(
				gameContext,
				lobbyManager,
				profile,
				new Callable() {
					@Override
					public Object call() {
						gameContext.setScreen(MultiplayerLobby.this);
						return null;
					}
				},
				lobbyEntry
		));
	}

	private void createRoom(String roomName) {
		gameContext.setScreen(new MultiplayerRoom(
				gameContext,
				lobbyManager,
				profile,
				new RoomOptions(roomName),
				new Callable() {
					@Override
					public Object call() {
						gameContext.setScreen(MultiplayerLobby.this);
						return null;
					}
				}
		));
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
	public void hide() {
		gameContext.setInputProcessor(null);
		lobbyManager.exitLobby();
	}

	@Override
	public void show() {
		gameContext.setInputProcessor(stage);
		centerCell.setActor(makeLoadingWidget());
		lobbyManager.enterLobby(profile, lobbyListener);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}

	@Override
	public void onBack() {
		onBack.call();
	}
}
