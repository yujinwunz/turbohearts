package com.demodu;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.demodu.gamelogic.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by yujinwunz on 17/03/2017.
 */

public class PlayerHand implements InputProcessor {

	Random random = new Random();

	private ArrayList<GdxCard> cards;
	private int x, y, width, height;

	private TextureAtlas atlas;
	private Skin skin;
	private TurboHearts turboHearts;

	private PlayHandler onPlay;
	private int maxCards;

	public PlayerHand(
			int maxCards,
			int x,
			int y,
			int width,
			int height,
			TurboHearts turboHearts,
			PlayHandler playHandler
	) {

		this.turboHearts = turboHearts;

		loadResources();

		this.onPlay = playHandler;
		this.maxCards = maxCards;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		TextureRegion region = atlas.findRegion(Assets.getCardName(Card.example));
		double cardAspectRatio = (double)region.getRegionWidth() / region.getRegionHeight();
		double cardWidth = height * cardAspectRatio;
		double cardSpacing = (width - cardWidth) / 13;

		this.cards = new ArrayList<GdxCard>();
	}

	public void loadResources() {
		TextureAtlas atlas = turboHearts.manager.get(Assets.CARD_ATLAS);
		this.atlas = atlas;
		skin = new Skin();
		skin.addRegions(atlas);
	}

	public void render(float delta, SpriteBatch batch) {

		batch.begin();
		double left = x;
		for (int i = 0; i < cards.size(); i++) {
			cards.get(i).render(delta, batch);
		}
		batch.end();
	}

	public void addCard(int index, final GdxCard c) {
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

	public int getHeight() {
		return height;
	}

	public GdxCard removeCard(Card c) {
		GdxCard retval = cards.remove(cards.indexOf(c));
		reposition();
		return retval;
	}

	public GdxCard findCard(Card c) {
		return cards.get(cards.indexOf(c));
	}

	private void reposition() {
		reposition(this.x, this.y, this.width, this.height);
	}

	public void reposition(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		TextureRegion region = atlas.findRegion(Assets.getCardName(Card.example));
		double cardAspectRatio = (double)region.getRegionWidth() / region.getRegionHeight();
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

	public GdxCard getCard(int index) {
		return cards.get(index);
	}

	public List<GdxCard> getUnmodifiableCards() {
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

	public interface PlayHandler {
		void play(GdxCard c);
	}
}
