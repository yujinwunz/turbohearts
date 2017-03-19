package com.demodu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.demodu.gamelogic.Card;
import com.demodu.gamelogic.ClientGameView;
import com.demodu.gamelogic.Config;
import com.demodu.gamelogic.GameState;
import com.demodu.gamelogic.MoveReporter;
import com.demodu.gamelogic.PlayerActor;

import org.omg.CORBA.portable.UnknownException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yujinwunz on 17/03/2017.
 */

public class TurboHeartsGame extends ScreenAdapter {

	TurboHearts turboHearts;
	GameState gameState;

	Object playerMoveLock = new Object();
	ArrayList<Card> playerMove;
	PlayerHand playerHand;

	ArrayList<GdxCard> onTable = new ArrayList<GdxCard>();

	public TurboHeartsGame(TurboHearts turboHearts) {
		this.turboHearts = turboHearts;

		ArrayList<Card> shuffled = new ArrayList<Card>(Card.getDeck());
		Collections.shuffle(shuffled);
		/* For now I'm just doing singleplayer. */
		PlayerActor[] players = new PlayerActor[4];
		final TurboHeartsGame me = this;

		playerHand = new PlayerHand(shuffled.subList(0, 13), 100, -50, 600, 140, turboHearts, new PlayerHand.PlayHandler() {
			@Override
			public void play(Card c) {
				me.playerMove = new ArrayList<Card>();
				me.playerMove.add(c);
				me.playerMoveLock.notify();
			}
		});

		players[0] = new PlayerActor() {
			@Override
			public List<Card> getMove(ClientGameView clientGameView, MoveReporter reporter) {

				try {
					playerMoveLock.wait();
				} catch (InterruptedException e) {
					throw new UnknownException(e);
				}

				return me.playerMove;
			}
		};

		gameState = new GameState(
				Config.standardGame,
				players,
				shuffled.toArray(new Card[shuffled.size()]
				)
		);

		startGameLogicLoop();
	}

	private void startGameLogicLoop() {

	}

	private void _gameLoopIteration() {

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.6f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void resize(int width, int height) {
		this.playerHand.reposition(width/8, -50, width*6/8, 140);
	}
}
