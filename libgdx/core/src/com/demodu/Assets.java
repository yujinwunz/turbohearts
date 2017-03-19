package com.demodu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.demodu.gamelogic.Card;

/**
 * Created by yujinwunz on 17/03/2017.
 */

public class Assets {
	public static String CARD_ATLAS = "cards/cards.pack.atlas";

	public static String getCardName(Card c) {
		return c.getRank().getFilenameVal() + c.getSuit().getFilenameVal();
	}

	public static void stage(AssetManager manager) {
		manager.load(CARD_ATLAS, TextureAtlas.class);
	}

	public static void load(AssetManager manager) {
		manager.finishLoading();
	}
}
