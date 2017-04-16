package com.demodu.turbohearts.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.demodu.turbohearts.GameContext;
import com.demodu.turbohearts.assets.Assets;
import com.demodu.turbohearts.gamelogic.Card;
import com.demodu.turbohearts.gwtcompat.Callable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

public class GdxCard extends Card {
	public static final double ANIMATION_DURATION = 0.2;
	public static final double SELECT_HEIGHT_MAX_RATIO = 0.2;

	double x, y, width, height, a;
	// For collision detection;
	Polygon renderedPolygon;
	// How much are we pushed up due to being selected?
	double selectHeight;
	private boolean isTouched;
	HashSet<Integer> onPointers = new HashSet<Integer>();

	public void setOnClick(Callable onClick) {
		this.onClick = onClick;
	}

	Callable onClick = null;

	GameContext gameContext;
	TextureRegion region;

	// z = angle
	Vector3 animationBeginCheckpoint;
	Vector3 animationTarget;
	double animationBeginTime;
	double timeElapsed;

	private State state;

	private boolean selected;
	private boolean charged;

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isCharged() {
		return charged;
	}

	public void setCharged(boolean charged) {
		this.charged = charged;
	}


	public GdxCard(Rank rank, Suit suit,
				   float x, float y, float height, float a,
				   GameContext gameContext,
				   State state) {
		super(rank, suit);
		this.gameContext = gameContext;
		TextureAtlas atlas = gameContext.getManager().get(Assets.CARD_ATLAS);
		this.x = x;
		this.y = y;
		this.a = a;
		this.height = height;
		this.timeElapsed = 0;
		this.animationTarget = null;
		this.renderedPolygon = null;
		this.selectHeight = 0;
		this.isTouched = false;
		this.state = state;
		TextureRegion region =
				atlas.findRegion(Assets.getCardName(Card.example));
		this.region = atlas.findRegion(Assets.getCardName(this));
		double aspectRatio = (double)region.getRegionWidth() / region.getRegionHeight();
		this.width = this.height * aspectRatio;
	}

	public void render(float delta, SpriteBatch batch) {
		timeElapsed += delta;

		if (this.animationTarget != null) {
			double effectiveElapsed = Math.min(timeElapsed, animationBeginTime + ANIMATION_DURATION);
			Vector3 interp1 =
					animationBeginCheckpoint.cpy().scl((float)(animationBeginTime + ANIMATION_DURATION - effectiveElapsed));
			Vector3 interp2 = animationTarget.cpy().scl((float)(effectiveElapsed - animationBeginTime));
			Vector3 interp = interp1.cpy().add(interp2).scl((float)(1./ANIMATION_DURATION));
			this.x = interp.x;
			this.y = interp.y;
			this.a = interp.z;
			if (timeElapsed > animationBeginTime + ANIMATION_DURATION) {
				this.animationTarget = null;
			}
		} else {
			correctAngle();
		}

		if ((this.isTouched && this.state == State.Enabled) || selected) {
			selectHeight = Math.min(
					height * SELECT_HEIGHT_MAX_RATIO,
					selectHeight + delta * SELECT_HEIGHT_MAX_RATIO * height / ANIMATION_DURATION * 4
			);
		} else {
			selectHeight = Math.max(
					0,
					selectHeight - delta * SELECT_HEIGHT_MAX_RATIO * height / ANIMATION_DURATION * 4
			);
		}

		float x = (float)(this.x);
		float y = (float)(this.y);
		x += Math.sin(a) * selectHeight;
		y += Math.cos(a) * selectHeight;

		Color prevColour = batch.getColor();
		if (this.state == State.Disabled) {
			batch.setColor(new Color(0.5f, 0.5f, 0.5f, 1));
		}

		if (charged && this.state == State.Disabled) {
			batch.setColor(new Color(0.5f, 0.3f, 0.3f, 1));
		} else if (charged) {
			batch.setColor(new Color(1.0f, 0.85f, 0.85f, 1));
		}

		batch.draw(
				region,
				(float)(x - width/2),
				(float)(y - height/2),
				(float)width/2,
				(float)height/2,
				(float)width,
				(float)height,
				1,
				1,
				-(float)(this.a * 180 / Math.PI)
		);
		batch.setColor(prevColour);

		this.renderedPolygon = new Polygon(new float[]{
				(float)(x - width/2), (float)(y - height/2),
				(float)(x - width/2), (float)(y + height/2),
				(float)(x + width/2), (float)(y + height/2),
				(float)(x + width/2), (float)(y - height/2)
		});
		this.renderedPolygon.rotate((float)a);
	}

	public void sendTo(float x, float y, float a) {
		correctAngle();
		animationBeginCheckpoint = new Vector3((float)this.x, (float)this.y, (float)this.a);
		animationBeginTime = timeElapsed;
		animationTarget = new Vector3(x, y, a);
	}

	public void correctAngle() {
		while (a > Math.PI) {
			a -= Math.PI*2;
		}
		while (a < -Math.PI) {
			a += Math.PI*2;
		}
	}

	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		Vector3 actual = gameContext.getCamera().unproject(new Vector3(screenX, screenY, 0));
		boolean retval = false;
		if (this.renderedPolygon != null && this.renderedPolygon.contains(actual.x, actual.y)) {
			onPointers.add(pointer);
			retval = true;
		} else {
			onPointers.remove(pointer);
		}
		this.isTouched = onPointers.size() > 0;
		return retval;
	}

	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 actual = gameContext.getCamera().unproject(new Vector3(screenX, screenY, 0));
		this.onPointers.remove(pointer);
		this.isTouched = onPointers.size() > 0;

		if (this.renderedPolygon.contains(actual.x, actual.y) && !this.isTouched) {
			if (this.state == State.Enabled) {
				try {
					onClick.call();
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					Gdx.app.error(
							"GdxCard", "Error in onclick, \"" + e.getLocalizedMessage() + "\" trace:\n"
									+ sw.toString()
					);
				}
			}
			return true;
		}
		return false;
	}

	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector3 actual = gameContext.getCamera().unproject(new Vector3(screenX, screenY, 0));
		boolean retval = false;
		if (this.renderedPolygon != null && this.renderedPolygon.contains(actual.x, actual.y)) {
			onPointers.add(pointer);
			retval = true;
		} else {
			onPointers.remove(pointer);
		}
		this.isTouched = onPointers.size() > 0;
		return retval;
	}

	public void touchDraggedOntoAnotherCard(int pointer) {
		onPointers.remove(pointer);
		this.isTouched = onPointers.size() > 0;
	}


	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}


	public enum State {
		// Clickable
		Enabled,
		// Dark and unclickable
		Disabled,
		// Bright but unclickable
		Inactive
	}
}
