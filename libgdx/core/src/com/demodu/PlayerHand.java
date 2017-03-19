package com.demodu;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.demodu.gamelogic.Card;

import java.util.ArrayList;
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

	private double timeLapsed;
	private PlayHandler onPlay;

	public PlayerHand(
			List<Card> cards,
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
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		TextureRegion region = atlas.findRegion(Assets.getCardName(Card.example));
		double cardAspectRatio = (double)region.getRegionWidth() / region.getRegionHeight();
		double cardWidth = height * cardAspectRatio;
		double cardSpacing = (width - cardWidth) / 13;

		this.cards = new ArrayList<GdxCard>();
		for (int i = 0; i < cards.size(); i++) {
			final int index = i;
			final PlayerHand me = this;
			final GdxCard newCard = new GdxCard(
					cards.get(i).getRank(), cards.get(i).getSuit(),
					(float)(x + cardWidth / 2 + cardSpacing * i),
					y + height / 2,
					height,
					0,
					turboHearts,
					GdxCard.State.Inactive
			);
			newCard.setOnClick(new Callable() {
				@Override
				public Object call() throws Exception {
					me.cards.remove(newCard);
					me.onPlay.play(newCard);
					me.reposition();
					return null;
				}
			});
			this.cards.add(newCard);
		}
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
		double cardSpacing = (width - cardWidth) / 12;

		double left = x + cardSpacing * (13 - cards.size())/2;
		for (GdxCard c : cards) {
			c.sendTo((float)(left + cardWidth / 2), y + height / 2, 0);
			c.height = height;
			c.width = cardWidth;
			left += cardSpacing;
		}

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
		void play(Card c);
	}
}
