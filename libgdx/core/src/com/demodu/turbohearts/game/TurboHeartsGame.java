package com.demodu.turbohearts.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.assets.AssetFactory;
import com.demodu.turbohearts.assets.Assets;
import com.demodu.turbohearts.crossplat.auth.Avatar;
import com.demodu.turbohearts.game.player.DelayedPlayer;
import com.demodu.turbohearts.gamelogic.Card;
import com.demodu.turbohearts.gamelogic.GameConductor;
import com.demodu.turbohearts.gamelogic.MoveReporter;
import com.demodu.turbohearts.gwtcompat.Callable;
import com.demodu.turbohearts.screens.Menu;
import com.demodu.turbohearts.screens.ScoreScreen;
import com.demodu.turbohearts.screens.TurboScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TurboHeartsGame extends TurboScreen {
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

	private Callable onGameEnd;

	MoveReporter moveReporter;
	ArrayList<com.demodu.turbohearts.game.GdxCard> onTable = new ArrayList<com.demodu.turbohearts.game.GdxCard>();
	ArrayList<com.demodu.turbohearts.game.GdxCard> otherChargedCards = new ArrayList<com.demodu.turbohearts.game.GdxCard>();
	boolean clearTableOnNextPlay = false;
	Avatar left, across, right;

	public TurboHeartsGame(
			final GameContext gameContext,
			GameConductor gameConductor,
			final Callable onGameEnd,
			Avatar left,
			Avatar across,
			Avatar right
	) {
		this.gameContext = gameContext;
		this.phase = Phase.Waiting;
		this.playerMove = new ArrayList<Card>();
		this.stage = new Stage(new StretchViewport(800, 480));
		this.inputProcessor = new InputMultiplexer();

		this.left = left;
		this.across = across;
		this.right = right;

		this.onGameEnd = onGameEnd;

		playerHand = new PlayerHand(13, 100, -50, 600, 140, gameContext, new PlayerHand.PlayHandler() {
			@Override
			public void play(com.demodu.turbohearts.game.GdxCard c) {
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
						try {
							moveReporter.reportMove(Collections.singletonList((Card) c));
						} catch (MoveReporter.InvalidMoveException ex) {
							throw new IllegalStateException("State is invalid", ex);
						}
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

		this.player = new UIPlayer(gameContext, this, new Callable() {
			@Override
			public Object call() {
				endGame(onGameEnd);
				return null;
			}
		});
		gameConductor.registerPlayer(player);
		inputProcessor.addProcessor(playerHand);
		inputProcessor.addProcessor(stage);
		gameContext.setInputProcessor(inputProcessor);
	}

	private void endGame(final Callable onGameEnd) {
		int self = 0, left = 0, across = 0, right = 0;
		for (ScoreScreen.RoundScore r: scores){
			self += r.getSelf();
			left += r.getLeft();
			across += r.getAcross();
			right += r.getRight();
		}
		final String message;
		if (left < self || across < self || right < self) {
			message = "Better luck next time!";
		} else {
			message = "You won!";
		}
		ScoreScreen scoreScreen = new ScoreScreen(scores, gameContext, new Callable() {
			@Override
			public Object call() {
				gameContext.setScreen(new Menu(
						message,
						gameContext,
						onGameEnd,
						new Menu.MenuItem("Ok", onGameEnd)
				));
				return null;
			}
		});
		gameContext.setScreen(scoreScreen);
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

	void addCards(List<com.demodu.turbohearts.game.GdxCard> cards) {
		for (com.demodu.turbohearts.game.GdxCard c : cards) {
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

	List<com.demodu.turbohearts.game.GdxCard> getCards() {
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
			playerHand.getCard(i).setState(com.demodu.turbohearts.game.GdxCard.State.Enabled);
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
			com.demodu.turbohearts.game.GdxCard gc = playerHand.removeCard(c);
			onTable.add(gc);
			gc.sendTo(flyTo.x, flyTo.y, 0);
		}
		clearTableOnNextPlay = true;
		ArrayList<Card> thisPlayerMove = new ArrayList<Card>(playerMove);
		playerMove.clear();
		enterWaiting();
		try {
			moveReporter.reportMove(thisPlayerMove);
		} catch (MoveReporter.InvalidMoveException ex) {
			throw new IllegalStateException("Cannot make pass", ex);
		}
	}

	void startCharging() {
		Gdx.app.log("Game", "UI should start charging");
		this.phase = Phase.Charging;
		boolean hasChargeableCards = false;
		for (int i = 0; i < playerHand.size(); i++) {
			if (Card.getChargeableCards().contains(playerHand.getCard(i))
					&& !playerHand.getCard(i).isCharged()) {
				hasChargeableCards = true;
				playerHand.getCard(i).setState(com.demodu.turbohearts.game.GdxCard.State.Enabled);
			} else {
				playerHand.getCard(i).setState(com.demodu.turbohearts.game.GdxCard.State.Disabled);
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
			com.demodu.turbohearts.game.GdxCard gc = playerHand.findCard(c);
			gc.setCharged(true);
			gc.setSelected(false);
			//otherChargedCards.add(playerHand.getCard(i));
		}

		ArrayList<Card> thisPlayerMove = new ArrayList<Card>(playerMove);
		playerMove.clear();
		enterWaiting();
		try {
			moveReporter.reportMove(thisPlayerMove);
		} catch (MoveReporter.InvalidMoveException ex) {
			throw new IllegalStateException("Could not do charge", ex);
		}
	}

	void startPlaying(List<Card> legalMoves) {
		this.phase = Phase.Playing;
		for (int i = 0; i < playerHand.size(); i++) {
			if (legalMoves.contains(playerHand.getCard(i))) {
				playerHand.getCard(i).setState(com.demodu.turbohearts.game.GdxCard.State.Enabled);
			} else {
				playerHand.getCard(i).setState(com.demodu.turbohearts.game.GdxCard.State.Disabled);
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
			playerHand.getCard(i).setState(com.demodu.turbohearts.game.GdxCard.State.Inactive);
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
		for (com.demodu.turbohearts.game.GdxCard c : onTable) {
			c.render(delta, gameContext.getSpriteBatch());
		}
		for (com.demodu.turbohearts.game.GdxCard c : otherChargedCards) {
			c.render(delta, gameContext.getSpriteBatch());
		}

		left.draw(gameContext, 0, 200, 200);
		across.draw(gameContext, getWidth() - 300, getHeight(), 200);
		right.draw(gameContext, getWidth() - 200, 200, 200);

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

	public void pauseGame() {
		gameContext.setScreen(
				new Menu(
						"Paused",
						gameContext,
						new Callable() {
							@Override
							public Object call() {
								resumeGame();
								return null;
							}
						},
						Menu.createMenuItem("Resume", new Callable() {
							@Override
							public Object call() {
								resumeGame();
								return null;
							}
						}),
						Menu.createMenuItem("Quit", new Callable() {
							@Override
							public Object call() {
								onGameEnd.call();
								return null;
							}
						})
				)
		);
	}

	public void resumeGame() {
		gameContext.setScreen(this);
	}


	@Override
	public void hide() {
		gameContext.setInputProcessor(null);
	}

	@Override
	public void show() {
		gameContext.setInputProcessor(this.inputProcessor);
	}

	@Override
	public void onBack() {
		pauseGame();
	}

	private enum Phase {
		Passing,
		Waiting,
		Charging,
		Playing
	}
}
