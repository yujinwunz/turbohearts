package com.demodu.turbohearts.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.assets.AssetFactory;
import com.demodu.turbohearts.assets.Assets;

import static com.demodu.turbohearts.assets.Assets.Colors.BACKGROUND_COLOUR_B;
import static com.demodu.turbohearts.assets.Assets.Colors.BACKGROUND_COLOUR_G;
import static com.demodu.turbohearts.assets.Assets.Colors.BACKGROUND_COLOUR_R;

/**
 * Created by yujinwunz on 27/03/2017.
 */

public class RegisterUsername extends TurboScreen {

	private Stage stage = new Stage(new StretchViewport(800, 480));
	private RegisterCallback registerCallback;
	private GameContext gameContext;

	public RegisterUsername(GameContext gameContext, final RegisterCallback registerCallback) {

		this.registerCallback = registerCallback;
		this.gameContext = gameContext;

		Table table = new Table();
		table.setFillParent(true);
		table.center();

		Skin skin = gameContext.getManager().get(Assets.UI_SKIN);

		final TextField usernameField = new TextField("", skin);
		final TextField displayNameField = new TextField("", skin);
		Label usernameLabel = new Label(
				"Username:",
				AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0)
		);
		Label displayNameLabel = new Label(
				"displayName:",
				AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0)
		);
		Label titleLabel = new Label(
				"One last step... choose a\nusername and display name!",
				AssetFactory.makeMediumLabelStyle(gameContext.getManager(), 1, 1, 0)
		);
		final Button registerButton = new TextButton(
				"Register",
				AssetFactory.makeMediumTextButtonStyle(gameContext.getManager(),
						Assets.Colors.BACKGROUND_COLOUR_R,
						Assets.Colors.BACKGROUND_COLOUR_G,
						Assets.Colors.BACKGROUND_COLOUR_B
				)
		);

		final Button cancelButton = new TextButton(
				"Cancel",
				AssetFactory.makeMediumTextButtonStyle(gameContext.getManager(),
						Assets.Colors.BACKGROUND_COLOUR_R,
						Assets.Colors.BACKGROUND_COLOUR_G,
						Assets.Colors.BACKGROUND_COLOUR_B
				)
		);

		registerButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				registerButton.setDisabled(true);
				cancelButton.setDisabled(true);
				registerCallback.onRegister(usernameField.getText(), displayNameField.getText());
			}
		});

		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				registerCallback.onCancel();
			}
		});

		table.add(titleLabel).colspan(2);
		table.row().pad(60);
		table.add(usernameLabel);
		table.add(usernameField).width(200);
		table.row().pad(20);
		table.add(displayNameLabel);
		table.add(displayNameField).width(200);
		table.row().pad(60);

		table.add(cancelButton);
		table.add(registerButton);

		stage.addActor(table);
		gameContext.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(BACKGROUND_COLOUR_R, BACKGROUND_COLOUR_G, BACKGROUND_COLOUR_B, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
	}

	@Override
	public void show() {
		gameContext.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		gameContext.setInputProcessor(null);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void onBack() {
		registerCallback.onCancel();
	}


	public interface RegisterCallback {
		void onRegister(String username, String displayName);
		void onCancel();
	}
}
