package com.demodu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TurboHearts extends Game {

	AssetManager manager;
	SpriteBatch spriteBatch;
	OrthographicCamera camera;
	Resources resources = new Resources();

	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);
		manager = new AssetManager();
		Assets.stage(manager);
		Assets.load(manager);

		setScreen(new TurboHeartsGame(this));
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		double aspectRatio = (double)width / height;
		camera.setToOrtho(false, (float)(480f * aspectRatio), 480);

		spriteBatch.setProjectionMatrix(camera.combined);
	}

	// Cannot be put into a static class
	class Resources {
		private static final int BUTTON_PADDING = 20;

		public TextButton.TextButtonStyle makeTextButtonStyle(float r, float g, float b) {
			Skin buttonSkin = new Skin();
			buttonSkin.addRegions(manager.get(Assets.BUTTON_ATLAS, TextureAtlas.class));
			for (String drawableName : new String[]{Assets.Button.BUTTON_UP, Assets.Button.BUTTON_DOWN, Assets.Button.BUTTON_CHECKED}) {
				Drawable d = buttonSkin.getDrawable(drawableName);
				d.setBottomHeight(BUTTON_PADDING);
				d.setLeftWidth(BUTTON_PADDING);
				d.setTopHeight(BUTTON_PADDING);
				d.setRightWidth(BUTTON_PADDING);
			}

			TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
			textButtonStyle.up = buttonSkin.getDrawable(Assets.Button.BUTTON_UP);
			textButtonStyle.down = buttonSkin.getDrawable(Assets.Button.BUTTON_DOWN);
			textButtonStyle.checked = buttonSkin.getDrawable(Assets.Button.BUTTON_CHECKED);
			textButtonStyle.checkedFontColor =
					new Color(r, g, b, 1);
			textButtonStyle.downFontColor =
					new Color(r / 3, g / 3, b / 3, 1);
			textButtonStyle.fontColor =
					new Color(r, g, b, 1);


			textButtonStyle.font = manager.get(Assets.FONT_LARGE);
			return textButtonStyle;
		}
	}

}
