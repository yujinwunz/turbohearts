package com.demodu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;

/**
 * Created by yujinwunz on 22/03/2017.
 */

public class ScoreScreen extends ScreenAdapter {
	private Stage stage;
	BitmapFont font;
	TurboHearts turboHearts;
	Callable onContinue;

	TextButton.TextButtonStyle textButtonStyle;

	private Label makeLabel(String str) {
		return new Label(str, new Label.LabelStyle(font, Color.PURPLE));
	}

	public ScoreScreen(ArrayList<RoundScore> scores, TurboHearts turboHearts, final Callable onContinue) {
		stage = new Stage(new StretchViewport(800, 480));
		Table table = new Table();
		table.setFillParent(true);
		table.top();

		font = turboHearts.manager.get(Assets.FONT_SMALL);
		textButtonStyle = turboHearts.resources.makeTextButtonStyle(0, 1.0f, 0.5f);

		table.add(makeLabel("Player")).pad(30);
		table.add(makeLabel("Me")).pad(30);
		table.add(makeLabel("Left")).pad(30);
		table.add(makeLabel("Across")).pad(30);
		table.add(makeLabel("Right")).pad(30);

		int selfTot = 0, leftTot = 0, acrossTot = 0, rightTot = 0;

		for (int i = 0; i < scores.size(); i++) {
			RoundScore score = scores.get(i);
			table.row();
			table.add(makeLabel("Round " + (i+1) + ":"));
			table.add(makeLabel(Integer.toString(score.getSelf())));
			table.add(makeLabel(Integer.toString(score.getLeft())));
			table.add(makeLabel(Integer.toString(score.getAcross())));
			table.add(makeLabel(Integer.toString(score.getRight())));
			selfTot += score.getSelf();
			leftTot += score.getLeft();
			acrossTot += score.getAcross();
			rightTot += score.getRight();
		}
		table.row();
		table.pad(20);
		table.add(makeLabel("Total:"));
		table.add(makeLabel(Integer.toString(selfTot)));
		table.add(makeLabel(Integer.toString(leftTot)));
		table.add(makeLabel(Integer.toString(acrossTot)));
		table.add(makeLabel(Integer.toString(rightTot)));
		table.row();

		// Continue button
		Button button = new TextButton("Continue", textButtonStyle);
		button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					onContinue.call();
				} catch (Exception e) {
					Gdx.app.error("ScoreScreen", "On continue error, ", e);
				}
			}
		});

		table.add(button).expand().bottom().right().colspan(5);

		stage.addActor(table);

		this.onContinue = onContinue;
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.0f, 1.0f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	static class RoundScore {
		int self, left, across, right;

		public RoundScore(int self, int left, int across, int right) {
			this.self = self;
			this.left = left;
			this.across = across;
			this.right = right;
		}

		public int getSelf() {
			return self;
		}

		public int getLeft() {
			return left;
		}

		public int getAcross() {
			return across;
		}

		public int getRight() {
			return right;
		}
	}
}
