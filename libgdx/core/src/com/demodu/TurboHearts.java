package com.demodu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.demodu.gamelogic.LocalGameConductor;
import com.demodu.player.RandomAI;

public class TurboHearts extends Game {

	AssetManager manager;
	SpriteBatch spriteBatch;
	OrthographicCamera camera;

	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);
		manager = new AssetManager();
		com.demodu.assets.Assets.stage(manager);
		com.demodu.assets.Assets.load(manager);

		setScreen(new TurboHeartsGame(this, new LocalGameConductor(
				new RandomAI(),
				new RandomAI(),
				new RandomAI()
		)));
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
}
