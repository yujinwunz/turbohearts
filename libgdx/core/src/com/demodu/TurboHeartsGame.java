package com.demodu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.Config;
import com.demodu.gamelogic.GameState;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gamelogic.PlayerActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yujinwunz on 17/03/2017.
 */

public class TurboHeartsGame extends ScreenAdapter {
	public static final int BUTTON_PADDING = 20;
	public static float BACKGROUND_COLOUR_R = 0.1f;
	public static float BACKGROUND_COLOUR_G = 0.4f;
	public static float BACKGROUND_COLOUR_B = 0.15f;

	int width = 800;
	int height = 480;

	TurboHearts turboHearts;
	GameState gameState;

	MoveReporter moveReporter;
	ArrayList<Card> playerMove;
	PlayerHand playerHand;
	DelayedPlayer player;

	Phase phase;
	Stage stage;
	Button passButton;
	InputMultiplexer inputProcessor;
	TextButton.TextButtonStyle textButtonStyle;

	ArrayList<GdxCard> onTable = new ArrayList<GdxCard>();
	ArrayList<GdxCard> otherChargedCards = new ArrayList<GdxCard>();
	boolean clearTableOnNextPlay = false;
	ArrayList<ScoreScreen.RoundScore> scores = new ArrayList<ScoreScreen.RoundScore>();

	public TurboHeartsGame(final TurboHearts turboHearts) {
		this.turboHearts = turboHearts;
		this.phase = Phase.Waiting;
		this.playerMove = new ArrayList<Card>();
		this.stage = new Stage(new StretchViewport(800, 480));
		this.inputProcessor = new InputMultiplexer();

		/* For now I'm just doing singleplayer. */
		PlayerActor[] players = new PlayerActor[4];
		final TurboHeartsGame me = this;

		playerHand = new PlayerHand(13, 100, -50, 600, 140, turboHearts, new PlayerHand.PlayHandler() {
			@Override
			public void play(GdxCard c) {
				switch (phase) {
					case Passing:
					case Charging:
						if (c.isSelected()) {
							c.setSelected(false);
							playerMove.remove(c);
						} else {
							// Can't pass more than 3 cards
							if (phase != Phase.Passing || playerMove.size() < 3) {
								c.setSelected(true);
								playerMove.add(c);
							}
						}

						if (phase == Phase.Passing) {
							if (playerMove.size() == 3) {
								passButton.setDisabled(false);
							} else {
								passButton.setDisabled(true);
							}
						}

						addCards(Collections.singletonList(c));
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

		players[0] = player = new UIPlayer(this);
		for (int i = 1; i < 4; i++) {
			players[i] = new RandomAI();
		}

		gameState = new GameState(
				Config.standardGame,
				players
		);

		textButtonStyle = turboHearts.resources.makeTextButtonStyle(
				BACKGROUND_COLOUR_R,
				BACKGROUND_COLOUR_G,
				BACKGROUND_COLOUR_B
		);
		gameState.start();
		inputProcessor.addProcessor(playerHand);
		inputProcessor.addProcessor(stage);
		Gdx.input.setInputProcessor(inputProcessor);
	}



	Vector2 getCoordinatesOfOpponent(GameState.PlayerPosition position) {
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

	void startPassing(GameState.Round passDirection) {
		Gdx.app.log("TurboHeartsGame", "startPassing");
		this.phase = Phase.Passing;
		for (int i = 0; i < playerHand.size(); i++) {
			playerHand.getCard(i).setState(GdxCard.State.Enabled);
		}

		Table table = new Table();
		table.setFillParent(true);
		table.center();

		Button button = new TextButton("Pass " + passDirection.toString(), textButtonStyle);
		button.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				doPass();
			}
		});
		table.add(button);

		button.setDisabled(true);
		passButton = button;
		stage.addActor(table);
	}

	private void doPass() {
		assert (playerMove.size() == 3);
		for (Card c : playerMove) {
			assert(playerHand.getUnmodifiableCards().contains(c));
			playerHand.removeCard(c);
		}
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
			if ((playerHand.getCard(i).equals(Card.ACE_OF_HEARTS) ||
					playerHand.getCard(i).equals(Card.QUEEN_OF_SPADES) ||
					playerHand.getCard(i).equals(Card.JACK_OF_DIAMONDS) ||
					playerHand.getCard(i).equals(Card.TEN_OF_CLUBS))
					&& !playerHand.getCard(i).isCharged()) {
				hasChargeableCards = true;
				playerHand.getCard(i).setState(GdxCard.State.Enabled);
			} else {
				playerHand.getCard(i).setState(GdxCard.State.Disabled);
			}
		}

		if (!hasChargeableCards) {
			moveReporter.reportMove(Collections.EMPTY_LIST);
			enterWaiting();
		} else {

			Table table = new Table();
			table.setFillParent(true);
			table.center();

			Button button = new TextButton("Charge cards", textButtonStyle);
			button.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					doCharge();
				}
			});
			table.add(button);
			stage.addActor(table);
		}
	}

	private void doCharge() {
		for (Card c : playerMove) {
			assert(playerHand.getUnmodifiableCards().contains(c));
			int i = playerHand.getUnmodifiableCards().indexOf(c);
			playerHand.getCard(i).setCharged(true);
			playerHand.getCard(i).setSelected(false);
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
		ScoreScreen scoreScreen = new ScoreScreen(scores, turboHearts, new Callable() {
			@Override
			public Object call() throws Exception {
				turboHearts.setScreen(me);
				return null;
			}
		});
		turboHearts.setScreen(scoreScreen);
	}

	private void enterWaiting() {
		Gdx.app.log("Game", "Entered waiting");
		for (int i = 0; i < playerHand.size(); i++) {
			playerHand.getCard(i).setState(GdxCard.State.Inactive);
		}
		this.stage.clear();
		this.phase = Phase.Waiting;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(BACKGROUND_COLOUR_R, BACKGROUND_COLOUR_G, BACKGROUND_COLOUR_B, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		this.playerHand.render(delta, turboHearts.spriteBatch);
		stage.act();
		stage.draw();
		turboHearts.spriteBatch.begin();
		for (GdxCard c : onTable) {
			c.render(delta, turboHearts.spriteBatch);
		}
		for (GdxCard c : otherChargedCards) {
			c.render(delta, turboHearts.spriteBatch);
		}
		turboHearts.spriteBatch.end();
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

	enum Phase {
		Passing,
		Waiting,
		Charging,
		Playing
	}
}
