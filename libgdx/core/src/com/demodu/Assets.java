package com.demodu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.demodu.gamelogic.Card;

/**
 * Created by yujinwunz on 17/03/2017.
 */

public class Assets {
	public static String CARD_ATLAS = "cards/cards.pack.atlas";
	public static String FONT_SMALL = "fonts/prstartk.fnt";
	public static String FONT_LARGE = "fonts/prstartk-large.fnt";
	public static String BUTTON_ATLAS = "buttons/buttons.pack.atlas";

	public static String getCardName(Card c) {
		return c.getRank().getFilenameVal() + c.getSuit().getFilenameVal();
	}

	public static void stage(AssetManager manager) {
		manager.load(BUTTON_ATLAS, TextureAtlas.class);
		manager.load(CARD_ATLAS, TextureAtlas.class);
		manager.load(FONT_SMALL, BitmapFont.class);
		manager.load(FONT_LARGE, BitmapFont.class);
	}

	public static void load(AssetManager manager) {
		manager.finishLoading();

	}

	public static class Button {
		public static String BUTTON_UP = "up-button";
		public static String BUTTON_DOWN = "down-button";
		public static String BUTTON_CHECKED = "checked-button";
	}
}
