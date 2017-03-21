package com.demodu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
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


	TurboHearts turboHearts;
	GameState gameState;

	MoveReporter moveReporter;
	ArrayList<Card> playerMove;
	PlayerHand playerHand;

	Phase phase;
	Stage stage;
	Button passButton;
	InputMultiplexer inputProcessor;
	TextButton.TextButtonStyle textButtonStyle;

	Skin buttonSkin = new Skin();

	ArrayList<GdxCard> onTable = new ArrayList<GdxCard>();

	public TurboHeartsGame(final TurboHearts turboHearts) {
		this.turboHearts = turboHearts;
		this.phase = Phase.Waiting;
		this.playerMove = new ArrayList<Card>();
		this.stage = new Stage();
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
						if (c.getState() == GdxCard.State.Selected) {
							c.setState(GdxCard.State.Enabled);
							playerMove.remove(c);
						} else {
							// Can't pass more than 3 cards
							if (phase != Phase.Passing || playerMove.size() < 3) {
								c.setState(GdxCard.State.Selected);
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
						moveReporter.reportMove(Collections.singletonList((Card)c));
						break;
					default:
						throw new UnsupportedOperationException("Cannot make a move while waiting");
				}
			}
		});

		players[0] = new PlayerActor() {
			@Override
			public void getMove(ClientGameView clientGameView, MoveReporter reporter) {
				switch (clientGameView.getGamePhase()) {
					case Passing:
						startPassing(clientGameView.getGameRound());
						break;
					case Charging:
						startCharging();
						break;
					case FirstRound:
					case Playing:
						startPlaying();
						break;
					default:
						phase = Phase.Waiting;
						break;
				}
				moveReporter = reporter;
			}

			@Override
			public void reportPlay(GameState.PlayerPosition position, Card card) {

			}

			@Override
			public void reportPass(GameState.PlayerPosition position, List<Card> cards) {
				Vector2 from = getCoordinatesOfOpponent(position);
				ArrayList<GdxCard> newCards = new ArrayList<GdxCard>();

				for (Card c: cards) {
					newCards.add(new GdxCard(
							c.getRank(),
							c.getSuit(),
							from.x,
							from.y,
							playerHand.getHeight(),
							0,
							turboHearts,
							GdxCard.State.Inactive
							)
					);
				}

				addCards(newCards);
			}

			@Override
			public void reportCharge(GameState.PlayerPosition position, Card card) {

			}
		};

		for (int i = 1; i < 4; i++) {
			players[i] = new RandomAI();
		}

		gameState = new GameState(
				Config.standardGame,
				players
		);

		prepareResources();
		gameState.start();
		inputProcessor.addProcessor(playerHand);
		inputProcessor.addProcessor(stage);
		Gdx.input.setInputProcessor(inputProcessor);
	}

	private void prepareResources() {
		buttonSkin.addRegions(turboHearts.manager.get(Assets.BUTTON_ATLAS, TextureAtlas.class));
		for (String drawableName: new String[]{Assets.Button.BUTTON_UP, Assets.Button.BUTTON_DOWN, Assets.Button.BUTTON_CHECKED}) {
			Drawable d = buttonSkin.getDrawable(drawableName);
			d.setBottomHeight(BUTTON_PADDING);
			d.setLeftWidth(BUTTON_PADDING);
			d.setTopHeight(BUTTON_PADDING);
			d.setRightWidth(BUTTON_PADDING);
		}

		textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = buttonSkin.getDrawable(Assets.Button.BUTTON_UP);
		textButtonStyle.down = buttonSkin.getDrawable(Assets.Button.BUTTON_DOWN);
		textButtonStyle.checked = buttonSkin.getDrawable(Assets.Button.BUTTON_CHECKED);
		textButtonStyle.checkedFontColor =
				new Color(BACKGROUND_COLOUR_R, BACKGROUND_COLOUR_G, BACKGROUND_COLOUR_B, 1);
		textButtonStyle.downFontColor =
				new Color(BACKGROUND_COLOUR_R/3, BACKGROUND_COLOUR_G/3, BACKGROUND_COLOUR_B/3, 1);
		textButtonStyle.fontColor =
				new Color(BACKGROUND_COLOUR_R, BACKGROUND_COLOUR_G, BACKGROUND_COLOUR_B, 1);


		textButtonStyle.font = turboHearts.manager.get(Assets.FONT_LARGE);

	}

	private Vector2 getCoordinatesOfOpponent(GameState.PlayerPosition position) {
		switch (position) {
			case Left:
				return new Vector2(0, Gdx.graphics.getHeight() / 2);
			case Across:
				return new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
			case Right:
				return new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2);
			default:
				return new Vector2(Gdx.graphics.getWidth()/2, 0);

		}
	}

	private void addCards(List<GdxCard> cards) {
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

	private void startPassing(GameState.Round passDirection) {
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
		moveReporter.reportMove(playerMove);
		playerMove.clear();
		enterWaiting();
	}

	private void startCharging() {
		Gdx.app.log("Game", "UI should start charging");
		this.phase = Phase.Charging;
		for (int i = 0; i < playerHand.size(); i++) {
			playerHand.getCard(i).setState(GdxCard.State.Enabled);
		}

		Table table = new Table();
		table.setFillParent(true);
		table.center();

		Button button = new TextButton("Charge cards", textButtonStyle);
		button.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				doCharge();
			}
		});
		table.add(button);

		button.setDisabled(true);
		passButton = button;
		stage.addActor(table);
	}

	private void doCharge() {

	}

	private void startPlaying() {

	}

	private void enterWaiting() {
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
	}

	@Override
	public void resize(int width, int height) {
		this.playerHand.reposition(width/8, -50, width*6/8, 140);
		this.stage.getViewport().update(width, height, true);
	}

	private enum Phase {
		Passing,
		Waiting,
		Charging,
		Playing
	}
}
