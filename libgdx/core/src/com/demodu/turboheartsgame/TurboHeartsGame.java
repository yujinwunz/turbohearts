package com.demodu.turboheartsgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.*;
import com.demodu.assets.AssetFactory;
import com.demodu.assets.Assets;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.GameConductor;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gwtcompat.Callable;
import com.demodu.player.DelayedPlayer;
import com.demodu.screens.ScoreScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TurboHeartsGame extends ScreenAdapter {
	private int width = 800;
	private int height = 480;

	private GameContext gameContext;

	private ArrayList<Card> playerMove;
	private PlayerHand playerHand;
	private DelayedPlayer player;

	private Phase phase;
	private Stage stage;
	private TextButton passButton;
	private TextButton chargeButton;
	private InputMultiplexer inputProcessor;
	private TextButton.TextButtonStyle textButtonStyle;

	private Texture background;

	private ArrayList<ScoreScreen.RoundScore> scores = new ArrayList<ScoreScreen.RoundScore>();

	MoveReporter moveReporter;
	ArrayList<GdxCard> onTable = new ArrayList<GdxCard>();
	ArrayList<GdxCard> otherChargedCards = new ArrayList<GdxCard>();
	boolean clearTableOnNextPlay = false;

	public TurboHeartsGame(final GameContext gameContext, GameConductor gameConductor) {
		this.gameContext = gameContext;
		this.phase = Phase.Waiting;
		this.playerMove = new ArrayList<Card>();
		this.stage = new Stage(new StretchViewport(800, 480));
		this.inputProcessor = new InputMultiplexer();

		playerHand = new PlayerHand(13, 100, -50, 600, 140, gameContext, new PlayerHand.PlayHandler() {
			@Override
			public void play(GdxCard c) {
				switch (phase) {
					case Passing:
						if (c.isSelected()) {
							c.setSelected(false);
							playerMove.remove(c);
						} else {
							// Can't pass more than 3 cards
							if (playerMove.size() < 3) {
								c.setSelected(true);
								playerMove.add(c);
							}
						}

						if (playerMove.size() == 3) {
							passButton.setDisabled(false);
						} else {
							passButton.setDisabled(true);
						}

						break;
					case Charging:
						if (c.isSelected()) {
							c.setSelected(false);
							playerMove.remove(c);
							chargeButton.setText("Charge (" + playerMove.size() + ") cards");
						} else {
							c.setSelected(true);
							playerMove.add(c);
							chargeButton.setText("Charge (" + playerMove.size() + ") cards");
						}
						break;
					case Playing:
						enterWaiting();
						moveReporter.reportMove(Collections.singletonList((Card)c));
						break;
					default:
						throw new UnsupportedOperationException("Cannot make a move while waiting");
				}
			}
		});


		textButtonStyle = AssetFactory.makeSmallTextButtonStyle(
				gameContext.getManager(),
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B
		);
		this.background = gameContext.getManager().get(Assets.BACKGROUND);
		background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		this.player = new UIPlayer(gameContext, this);
		gameConductor.registerPlayer(player);
		inputProcessor.addProcessor(playerHand);
		inputProcessor.addProcessor(stage);
		Gdx.input.setInputProcessor(inputProcessor);
	}

	Vector2 getCoordinatesOfOpponent(GameConductor.PlayerPosition position) {
		switch (position) {
			case Left:
				return new Vector2(-100, this.height / 2);
			case Across:
				return new Vector2(this.width / 2, this.height + 100);
			case Right:
				return new Vector2(this.width + 100, this.height / 2);
			default:
				return new Vector2(this.width/2, -100);

		}
	}

	void addCards(List<GdxCard> cards) {
		for (GdxCard c : cards) {
			// Sort them in the CORRECT order.

			int i = 0;
			for (; i < playerHand.size(); i++) {
				if (playerHand.getCard(i).getSuit().ordinal() > c.getSuit().ordinal()) {
					break;
				}
				if (playerHand.getCard(i).getSuit().ordinal() == c.getSuit().ordinal()) {
					if (playerHand.getCard(i).getRank().ordinal() > c.getRank().ordinal()) {
						break;
					}
				}
			}
			playerHand.addCard(i, c);
		}
	}

	List<GdxCard> getCards() {
		return playerHand.getUnmodifiableCards();
	}

	PlayerHand getPlayerHand() {
		return playerHand;
	}

	int getWidth() {
		return width;
	}

	int getHeight() {
		return height;
	}

	void startPassing(final GameConductor.Round passDirection) {
		Gdx.app.log("TurboHeartsGame", "startPassing");
		this.phase = Phase.Passing;
		for (int i = 0; i < playerHand.size(); i++) {
			playerHand.getCard(i).setState(GdxCard.State.Enabled);
		}

		Table table = new Table();
		table.setFillParent(true);
		table.center();

		TextButton button = new TextButton("Pass 3 " + passDirection.toString(), textButtonStyle);
		button.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				doPass(passDirection);
			}
		});
		table.add(button);

		button.setDisabled(true);
		passButton = button;
		stage.addActor(table);
	}

	private void doPass(GameConductor.Round passDirection) {
		Vector2 flyTo = getCoordinatesOfOpponent(GameConductor.PlayerPosition.roundPassTo(passDirection));
		for (Card c : playerMove) {
			GdxCard gc = playerHand.removeCard(c);
			onTable.add(gc);
			gc.sendTo(flyTo.x, flyTo.y, 0);
		}
		clearTableOnNextPlay = true;
		ArrayList<Card> thisPlayerMove = new ArrayList<Card>(playerMove);
		playerMove.clear();
		enterWaiting();
		moveReporter.reportMove(thisPlayerMove);
	}

	void startCharging() {
		Gdx.app.log("Game", "UI should start charging");
		this.phase = Phase.Charging;
		boolean hasChargeableCards = false;
		for (int i = 0; i < playerHand.size(); i++) {
			if (Card.getChargeableCards().contains(playerHand.getCard(i))
					&& !playerHand.getCard(i).isCharged()) {
				hasChargeableCards = true;
				playerHand.getCard(i).setState(GdxCard.State.Enabled);
			} else {
				playerHand.getCard(i).setState(GdxCard.State.Disabled);
			}
		}

		String buttonText;
		String helpText = "";
		if (otherChargedCards.size() > 0) {
			helpText = "An opponent has charged. You may react with more charging.";
		}
		if (hasChargeableCards) {
			buttonText = "Charge (0) cards";

		} else {
			buttonText = "Finish \"charging\"";
			helpText = "This button avoids timing tells when you can't charge.\n\n" + helpText;
		}


		Table table = new Table();
		table.setFillParent(true);
		table.center();

		chargeButton = new TextButton(buttonText, textButtonStyle);
		chargeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				doCharge();
			}
		});
		table.add(chargeButton);
		table.row();
		Label helpTextLabel = new Label(
				helpText,
				AssetFactory.makeSmallLabelStyle(gameContext.getManager(), 1, 1, 0)
		);
		helpTextLabel.setWrap(true);
		table.add(helpTextLabel).fillX();
		stage.addActor(table);
	}

	private void doCharge() {
		for (Card c : playerMove) {
			GdxCard gc = playerHand.findCard(c);
			gc.setCharged(true);
			gc.setSelected(false);
			//otherChargedCards.add(playerHand.getCard(i));
		}

		ArrayList<Card> thisPlayerMove = new ArrayList<Card>(playerMove);
		playerMove.clear();
		enterWaiting();
		moveReporter.reportMove(thisPlayerMove);
	}

	void startPlaying(List<Card> legalMoves) {
		this.phase = Phase.Playing;
		for (int i = 0; i < playerHand.size(); i++) {
			if (legalMoves.contains(playerHand.getCard(i))) {
				playerHand.getCard(i).setState(GdxCard.State.Enabled);
			} else {
				playerHand.getCard(i).setState(GdxCard.State.Disabled);
			}
		}
	}

	void roundEnd(int selfScore, int leftScore, int acrossScore, int rightScore) {
		final TurboHeartsGame me = this;
		scores.add(new ScoreScreen.RoundScore(selfScore, leftScore, acrossScore, rightScore));
		ScoreScreen scoreScreen = new ScoreScreen(scores, gameContext, new Callable() {
			@Override
			public Object call() {
				gameContext.setScreen(me);
				return null;
			}
		});
		gameContext.setScreen(scoreScreen);
	}

	void enterWaiting() {
		Gdx.app.log("Game", "Entered waiting");
		for (int i = 0; i < playerHand.size(); i++) {
			playerHand.getCard(i).setState(GdxCard.State.Inactive);
		}
		this.stage.clear();
		this.phase = Phase.Waiting;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(
				Assets.Colors.BACKGROUND_COLOUR_R,
				Assets.Colors.BACKGROUND_COLOUR_G,
				Assets.Colors.BACKGROUND_COLOUR_B,
				1
		);

		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		gameContext.getSpriteBatch().begin();
		gameContext.getSpriteBatch().draw(background, 0, 0, 0, 0, width, height);
		gameContext.getSpriteBatch().end();

		this.playerHand.render(delta, gameContext.getSpriteBatch());
		stage.act();
		stage.draw();

		gameContext.getSpriteBatch().begin();
		for (GdxCard c : onTable) {
			c.render(delta, gameContext.getSpriteBatch());
		}
		for (GdxCard c : otherChargedCards) {
			c.render(delta, gameContext.getSpriteBatch());
		}
		gameContext.getSpriteBatch().end();
		player.incrementTime(delta);
	}

	@Override
	public void resize(int width, int height) {
		this.stage.getViewport().update(width, height, false);
		double aspectRatio = (double)width / height;
		this.width = (int)(480f * aspectRatio);
		this.height = 480;
		this.playerHand.reposition(this.width/8, -50, this.width*6/8, 140);
		
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this.inputProcessor);
	}

	private enum Phase {
		Passing,
		Waiting,
		Charging,
		Playing
	}
}
