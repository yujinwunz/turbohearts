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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AssetFactory {
	private static final int BUTTON_PADDING = 10;

	public static TextButton.TextButtonStyle makeSmallTextButtonStyle(
			AssetManager manager,
			float r,
			float g,
			float b
	) {
		return makeTextButtonStyle(manager, r, g, b, manager.get(Assets.FONT_SMALL, BitmapFont.class), 5);
	}

	public static TextButton.TextButtonStyle makeMediumTextButtonStyle(
			AssetManager manager,
			float r,
			float g,
			float b
	) {
		return makeTextButtonStyle(manager, r, g, b, manager.get(Assets.FONT_MEDIUM, BitmapFont.class), 12);
	}

	public static TextButton.TextButtonStyle makeLargeTextButtonStyle(
			AssetManager manager,
			float r,
			float g,
			float b
	) {
		return makeTextButtonStyle(manager, r, g, b, manager.get(Assets.FONT_LARGE, BitmapFont.class), 20);
	}


	private static TextButton.TextButtonStyle makeTextButtonStyle(
			AssetManager manager,
			float r,
			float g,
			float b,
			BitmapFont font,
			float padding
	) {
		Skin buttonSkin = new Skin();
		buttonSkin.addRegions(
				manager.get(Assets.BUTTON_ATLAS, TextureAtlas.class));

		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up =
				new TextureRegionDrawable(buttonSkin.getRegion(Assets.Button.BUTTON_UP));
		textButtonStyle.down =
				new TextureRegionDrawable(buttonSkin.getRegion(Assets.Button.BUTTON_DOWN));
		textButtonStyle.checked =
				new TextureRegionDrawable(buttonSkin.getRegion(Assets.Button.BUTTON_CHECKED));
		textButtonStyle.disabled =
				new TextureRegionDrawable(buttonSkin.getRegion(Assets.Button.BUTTON_DOWN));

		setDrawablePadding(textButtonStyle.up, padding);
		setDrawablePadding(textButtonStyle.down, padding);
		setDrawablePadding(textButtonStyle.checked, padding);
		setDrawablePadding(textButtonStyle.disabled, padding);

		textButtonStyle.checkedFontColor = new Color(r, g, b, 1);
		textButtonStyle.downFontColor = new Color(r / 3, g / 3, b / 3, 1);
		textButtonStyle.fontColor = new Color(r, g, b, 1);

		textButtonStyle.font = font;
		return textButtonStyle;
	}

	private static void setDrawablePadding(Drawable d, float padding) {
		d.setBottomHeight(padding);
		d.setLeftWidth(padding);
		d.setTopHeight(padding);
		d.setRightWidth(padding);
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

	public static Label.LabelStyle makeMediumLabelStyle(AssetManager manager, float r, float g, float b) {
		return new Label.LabelStyle(
				manager.get(Assets.FONT_MEDIUM, BitmapFont.class),
				new Color(r, g, b, 1)
		);
	}
	public static Label.LabelStyle makeLargeLabelStyle(AssetManager manager, float r, float g, float b) {
		return new Label.LabelStyle(
				manager.get(Assets.FONT_LARGE, BitmapFont.class),
				new Color(r, g, b, 1)
		);
	}

}
