package com.demodu.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.demodu.gamelogic.Card;

public class Assets {
	public static String CARD_ATLAS = "cards/cards.pack.atlas";
	public static String FONT_SMALL = "fonts/prstartk-small.fnt";
	public static String FONT_MEDIUM = "fonts/prstartk.fnt";
	public static String FONT_LARGE = "fonts/prstartk-large.fnt";
	public static String BUTTON_ATLAS = "buttons/buttons.pack.atlas";
	public static String SCROLL_ATLAS = "scroll/scroll.pack.atlas";

	public static String getCardName(Card c) {
		return c.getRank().getFilenameVal() + c.getSuit().getFilenameVal();
	}

	public static void stage(AssetManager manager) {
		manager.load(BUTTON_ATLAS, TextureAtlas.class);
		manager.load(CARD_ATLAS, TextureAtlas.class);
		manager.load(SCROLL_ATLAS, TextureAtlas.class);
		manager.load(FONT_SMALL, BitmapFont.class);
		manager.load(FONT_MEDIUM, BitmapFont.class);
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

	public static class Scroll {
		public static String VSCROLL = "vscroll";
		public static String VSCROLL_KNOB = "vscrollknob";
	}

	public static class Colors {
		public static float BACKGROUND_COLOUR_R = 0.1f;
		public static float BACKGROUND_COLOUR_G = 0.4f;
		public static float BACKGROUND_COLOUR_B = 0.15f;
	}
}
