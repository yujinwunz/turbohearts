package com.demodu;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.demodu.assets.Assets;
import com.demodu.gamelogic.Card;
import com.demodu.gwtcompat.Callable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PlayerHand implements InputProcessor {
	private ArrayList<GdxCard> cards = new ArrayList<GdxCard>();;
	private int x, y, width, height;

	private PlayHandler onPlay;
	private int maxCards;
	private double cardAspectRatio;

	PlayerHand(
			int maxCards,
			int x,
			int y,
			int width,
			int height,
			GameContext gameContext,
			PlayHandler playHandler
	) {
		this.onPlay = playHandler;
		this.maxCards = maxCards;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		TextureAtlas atlas = gameContext.getManager().get(Assets.CARD_ATLAS);
		TextureRegion region = atlas.findRegion(Assets.getCardName(Card.example));
		cardAspectRatio = (double)region.getRegionWidth() / region.getRegionHeight();
	}

	void render(float delta, SpriteBatch batch) {

		batch.begin();
		for (int i = 0; i < cards.size(); i++) {
			cards.get(i).render(delta, batch);
		}
		batch.end();
	}

	void addCard(int index, final GdxCard c) {
		cards.add(index, c);
		c.setOnClick(new Callable() {
			@Override
			public Object call() throws Exception {
				onPlay.play(c);
				return null;
			}
		});
		reposition();
	}

	int getHeight() {
		return height;
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	GdxCard removeCard(Card c) {
		GdxCard retval = cards.remove(cards.indexOf(c));
		reposition();
		return retval;
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	GdxCard findCard(Card c) {
		return cards.get(cards.indexOf(c));
	}

	private void reposition() {
		reposition(this.x, this.y, this.width, this.height);
	}

	void reposition(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		double cardWidth = height * cardAspectRatio;
		double cardSpacing = (width - cardWidth) / (maxCards - 1);

		double left = x + cardSpacing * (maxCards - cards.size())/2;
		for (GdxCard c : cards) {
			c.sendTo((float)(left + cardWidth / 2), y + height / 2, 0);
			c.height = height;
			c.width = cardWidth;
			left += cardSpacing;
		}

	}

	GdxCard getCard(int index) {
		return cards.get(index);
	}

	List<GdxCard> getUnmodifiableCards() {
		return Collections.unmodifiableList(cards);
	}

	public int size() {
		return cards.size();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		for (int i = cards.size() - 1; i >= 0; i--) {
			if (cards.get(i).touchDown(screenX, screenY, pointer, button)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		for (int i = cards.size() - 1; i >= 0; i--) {
			if (cards.get(i).touchUp(screenX, screenY, pointer, button)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		boolean foundCard = false;
		for (int i = cards.size() - 1; i >= 0; i--) {
			if (foundCard) {
				cards.get(i).touchDraggedOntoAnotherCard(pointer);
			} else if (cards.get(i).touchDragged(screenX, screenY, pointer)) {
				foundCard = true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	interface PlayHandler {
		void play(GdxCard c);
	}
}
