package com.demodu.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class AssetFactory {
	private static final int BUTTON_PADDING = 20;

	public static TextButton.TextButtonStyle makeTextButtonStyle(
			AssetManager manager,
			float r,
			float g,
			float b
	) {
		Skin buttonSkin = new Skin();
		buttonSkin.addRegions(
				manager.get(Assets.BUTTON_ATLAS, TextureAtlas.class));
		for (String drawableName : new String[]{
				Assets.Button.BUTTON_UP,
				Assets.Button.BUTTON_DOWN,
				Assets.Button.BUTTON_CHECKED
		}) {
			Drawable d = buttonSkin.getDrawable(drawableName);
			d.setBottomHeight(BUTTON_PADDING);
			d.setLeftWidth(BUTTON_PADDING);
			d.setTopHeight(BUTTON_PADDING);
			d.setRightWidth(BUTTON_PADDING);
		}

		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = buttonSkin.getDrawable(Assets.Button.BUTTON_UP);
		textButtonStyle.down = buttonSkin.getDrawable(Assets.Button.BUTTON_DOWN);
		textButtonStyle.checked =
				buttonSkin.getDrawable(Assets.Button.BUTTON_CHECKED);
		textButtonStyle.disabled =
				buttonSkin.getDrawable(Assets.Button.BUTTON_DOWN);
		textButtonStyle.checkedFontColor =
				new Color(r, g, b, 1);
		textButtonStyle.downFontColor =
				new Color(r / 3, g / 3, b / 3, 1);
		textButtonStyle.fontColor =
				new Color(r, g, b, 1);

		textButtonStyle.font = manager.get(Assets.FONT_MEDIUM);
		return textButtonStyle;
	}

	public static ScrollPane.ScrollPaneStyle makeScrollPaneStyle(AssetManager manager) {
		ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
		Skin skin = new Skin();
		skin.addRegions(manager.get(Assets.SCROLL_ATLAS, TextureAtlas.class));
		scrollPaneStyle.vScroll = skin.getDrawable(Assets.Scroll.VSCROLL);
		scrollPaneStyle.vScrollKnob =
				skin.getDrawable(Assets.Scroll.VSCROLL_KNOB);

		return scrollPaneStyle;
	}

	public static Label.LabelStyle makeSmallLabelStyle(AssetManager manager, float r, float g, float b) {
		return new Label.LabelStyle(
				manager.get(Assets.FONT_SMALL, BitmapFont.class),
				new Color(r, g, b, 1)
		);
	}
}
