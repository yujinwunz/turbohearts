package com.demodu.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.GameContext;
import com.demodu.assets.AssetFactory;
import com.demodu.assets.Assets;
import com.demodu.gwtcompat.Callable;

import static com.demodu.assets.Assets.Colors.BACKGROUND_COLOUR_B;
import static com.demodu.assets.Assets.Colors.BACKGROUND_COLOUR_G;
import static com.demodu.assets.Assets.Colors.BACKGROUND_COLOUR_R;

public class Menu extends ScreenAdapter {
	private Stage stage;

	public Menu(String title, GameContext gameContext, MenuItem... menuItems) {

		stage = new Stage(new StretchViewport(800, 480));
		com.badlogic.gdx.scenes.scene2d.ui.Table table = new Table();
		table.setFillParent(true);
		table.center();

		Label titleLabel = new Label(title, AssetFactory.makeMediumLabelStyle(
				gameContext.getManager(),
				1,
				1,
				0
		));
		titleLabel.setAlignment(Align.center);

		table.add(titleLabel);
		table.row().padBottom(20f);

		TextButton.TextButtonStyle textButtonStyle = AssetFactory.makeMediumTextButtonStyle(
				gameContext.getManager(),
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B
		);

		for (MenuItem m : menuItems) {
			final MenuItem final_m = m;
			Button button = new TextButton(m.text, textButtonStyle);
			button.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					try {
						final_m.callable.call();
					} catch (Exception e) {
						Gdx.app.error("Menu", "Menu item exception", e);
					}
				}
			});
			table.add(button).pad(20f);
			table.row();
		}
		stage.addActor(table);
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
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	public static class MenuItem {
		public String text;
		public Callable callable;
		public boolean isMain = false;

		public MenuItem(String text, Callable callable) {
			this(text, callable, false);
		}
		public MenuItem(String text, Callable callable, boolean isMain) {
			this.text = text;
			this.callable = callable;
			this.isMain = isMain;
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public static MenuItem createMenuItem(String text, Callable callable) {
		return new MenuItem(text, callable);
	}
	public static MenuItem createMenuItem(String text, Callable callable, boolean isMain) {
		return new MenuItem(text, callable, isMain);
	}
}


