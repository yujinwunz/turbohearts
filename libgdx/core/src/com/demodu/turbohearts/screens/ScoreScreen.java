package com.demodu.turbohearts.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.assets.AssetFactory;
import com.demodu.turbohearts.assets.Assets;
import com.demodu.turbohearts.gwtcompat.Callable;

import java.util.ArrayList;

public class ScoreScreen extends TurboScreen {
	private Stage stage;
	private BitmapFont font;
	private Callable onContinue;
	private GameContext gameContext;

	private Label makeLabel(String str) {
		Label label = new Label(str, new Label.LabelStyle(font, Color.YELLOW));
		label.setAlignment(Align.left);
		return label;
	}

	public ScoreScreen(
			ArrayList<RoundScore> scores,
			GameContext gameContext,
			final Callable onContinue
	) {
		this.onContinue = onContinue;
		this.gameContext = gameContext;

		stage = new Stage(new StretchViewport(800, 480));
		Table table = new Table();
		table.setFillParent(true);

		font = gameContext.getManager().get(Assets.FONT_SMALL);
		TextButton.TextButtonStyle textButtonStyle = AssetFactory.makeSmallTextButtonStyle(
				gameContext.getManager(),
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B
		);

		float w1 = table.add(makeLabel("Player:  ")).pad(15).getPrefWidth();
		float w2 = table.add(makeLabel("Me    ")).pad(15).getPrefWidth();
		float w3 = table.add(makeLabel("Left  ")).pad(15).getPrefWidth();
		float w4 = table.add(makeLabel("Across")).pad(15).getPrefWidth();
		float w5 = table.add(makeLabel("Right ")).pad(15).getPrefWidth();
		table.row();

		Table innerTable = new Table();
		innerTable.top();

		int selfTot = 0, leftTot = 0, acrossTot = 0, rightTot = 0;

		for (int i = 0; i < scores.size(); i++) {
			RoundScore score = scores.get(i);
			innerTable.add(makeLabel("Round " + (i+1) + ":")).width(w1).padLeft(15).padRight(15);
			innerTable.add(makeLabel(Integer.toString(score.getSelf()))).width(w2).padLeft(15).padRight(15);
			innerTable.add(makeLabel(Integer.toString(score.getLeft()))).width(w3).padLeft(15).padRight(15);
			innerTable.add(makeLabel(Integer.toString(score.getAcross()))).width(w4).padLeft(15).padRight(15);
			innerTable.add(makeLabel(Integer.toString(score.getRight()))).width(w5).padLeft(15).padRight(15);
			selfTot += score.getSelf();
			leftTot += score.getLeft();
			acrossTot += score.getAcross();
			rightTot += score.getRight();
			innerTable.row();
		}
		innerTable.row().colspan(5).expand().padBottom(30);
		innerTable.setFillParent(true);

		ScrollPane.ScrollPaneStyle scrollPaneStyle = AssetFactory.makeScrollPaneStyle(gameContext.getManager());
		ScrollPane scrollPane = new ScrollPane(innerTable, scrollPaneStyle);

		table.add(scrollPane).colspan(5);
		table.row();
		table.add(makeLabel("Total:")).fillX().pad(15);
		table.add(makeLabel(Integer.toString(selfTot))).fillX().pad(15);
		table.add(makeLabel(Integer.toString(leftTot))).fillX().pad(15);
		table.add(makeLabel(Integer.toString(acrossTot))).fillX().pad(15);
		table.add(makeLabel(Integer.toString(rightTot))).fillX().pad(15);
		table.row();

		// Continue button
		Button button = new TextButton("Continue", textButtonStyle);
		button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				onContinue.call();
			}
		});


		table.add(button).right().bottom().expandY().colspan(5);

		stage.addActor(table);
		gameContext.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B,
				1
		);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void onBack() {
		onContinue.call();
	}

	@Override
	public void hide() {
		gameContext.setInputProcessor(null);
	}

	@Override
	public void show() {
		gameContext.setInputProcessor(stage);
	}

	public static class RoundScore {
		private int self, left, across, right;

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
