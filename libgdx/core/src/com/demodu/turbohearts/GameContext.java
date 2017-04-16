package com.demodu.turbohearts;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by yujinwunz on 26/03/2017.
 */

public interface GameContext {
	AssetManager getManager();

	SpriteBatch getSpriteBatch();

	OrthographicCamera getCamera();

	void setScreen(Screen screen);
}
