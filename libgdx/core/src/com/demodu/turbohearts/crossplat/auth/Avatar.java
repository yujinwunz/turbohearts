package com.demodu.turbohearts.crossplat.auth;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;
import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.assets.Assets;

public class Avatar {
	private String displayName;
	private BitmapFont font = null;

	public String getDisplayName() {
		return displayName;
	}

	public Avatar(String displayName) {

		this.displayName = displayName;
	}

	public void draw(GameContext gameContext, float x, float y, float width) {
		if (font == null) {
			font = gameContext.getManager().get(Assets.FONT_SMALL);
		}
		font.draw(gameContext.getSpriteBatch(), displayName, x, y, width, Align.center, true);
	}
}
