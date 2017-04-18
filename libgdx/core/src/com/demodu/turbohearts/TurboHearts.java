package com.demodu.turbohearts;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.demodu.turbohearts.assets.Assets;
import com.demodu.turbohearts.crossplat.auth.AuthManager;
import com.demodu.turbohearts.crossplat.auth.Avatar;
import com.demodu.turbohearts.crossplat.auth.ExampleAuthManager;
import com.demodu.turbohearts.crossplat.auth.Profile;
import com.demodu.turbohearts.crossplat.content.ContentManager;
import com.demodu.turbohearts.crossplat.content.ExampleContentManager;
import com.demodu.turbohearts.crossplat.lobby.ExampleLobbyManager;
import com.demodu.turbohearts.crossplat.lobby.LobbyManager;
import com.demodu.turbohearts.game.TurboHeartsGame;
import com.demodu.turbohearts.game.player.RandomAI;
import com.demodu.turbohearts.gamelogic.LocalGameConductor;
import com.demodu.turbohearts.gwtcompat.Callable;
import com.demodu.turbohearts.screens.LoginSelection;
import com.demodu.turbohearts.screens.MainMenu;
import com.demodu.turbohearts.screens.Menu;
import com.demodu.turbohearts.screens.MultiplayerLobby;
import com.demodu.turbohearts.screens.RegisterUsername;
import com.demodu.turbohearts.screens.TurboScreen;

import java.util.Arrays;

public class TurboHearts extends Game implements GameContext, InputProcessor {

	private AssetManager manager;
	private SpriteBatch spriteBatch;
	private OrthographicCamera camera;

	private AuthManager authManager;
	private ContentManager contentManager;
	private LobbyManager lobbyManager;

	private TurboScreen currentTurboScreen;
	private InputMultiplexer inputMultiplexer = new InputMultiplexer(this);

	public TurboHearts(
			AuthManager authManager,
			ContentManager contentManager,
			LobbyManager lobbyManager
	) {
		this.authManager = authManager;
		this.contentManager = contentManager;
		this.lobbyManager = lobbyManager;
	}

	public TurboHearts() {
		this(
				new ExampleAuthManager(),
				new ExampleContentManager(),
				new ExampleLobbyManager()
		);
	}

	@Override
	public void create () {
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(inputMultiplexer);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);
		manager = new AssetManager();
		Assets.stage(manager);
		Assets.load(manager);

		startMainMenu();
	}

	private void startMainMenu() {
		Gdx.app.log(
				"TurboHearts",
				"startMainMenu called from " +
						Arrays.toString(Thread.currentThread().getStackTrace())
		);
		setScreen(new MainMenu(
				this,
				new Callable() {
					@Override
					public Object call() {
						Gdx.app.exit();
						return null;
					}
				},
				new Callable() {
					@Override
					public Object call() {
						singlePlayer();
						return null;
					}
				},
				new Callable() {
					@Override
					public Object call() {
						multiPlayer();
						return null;
					}
				},
				new Callable() {
					@Override
					public Object call() {
						// TODO
						return null;
					}
				}
		));
	}

	private void singlePlayer() {
		setScreen(new TurboHeartsGame(
				this,
				new LocalGameConductor(
					new RandomAI(),
					new RandomAI(),
					new RandomAI()
				),
				new Callable() {
					@Override
					public Object call() {
						startMainMenu();
						return null;
					}
				},
				new Avatar("Light Blue (Random AI)"),
				new Avatar("HAL 8999 (Random AI)"),
				new Avatar("Crapratus (Random AI)")));
	}

	private void withRegistration(final AuthManager authManager, final Callable success, final Callable fail) {
		if (authManager.getCurrentLogin().getUsername() == null) {
			setScreen(new RegisterUsername(TurboHearts.this, new RegisterUsername.RegisterCallback() {
				@Override
				public void onRegister(String username, String displayName) {
					authManager.register(username, displayName, new AuthManager.LoginCallback() {
						@Override
						public void onFailure(String message) {
							fail.call();
						}

						@Override
						public void onSuccess(Profile profile) {
							success.call();
						}
					});
				}

				@Override
				public void onCancel() {
					fail.call();
				}
			}));
		} else {
			success.call();
		}
	}

	private void loginAndDo(AuthManager.LoginMethod loginMethod, final Callable success, final Callable fail) {
		authManager.startLogin(loginMethod, new AuthManager.LoginCallback() {
			@Override
			public void onSuccess(Profile profile) {
				withRegistration(authManager, success, fail);
			}
			@Override
			public void onFailure(String message) {
				Gdx.app.log("TurboHearts", "login failed " + message);
				setScreen(new Menu(
						"Login failed: " + message,
						TurboHearts.this,
						fail,
						new Menu.MenuItem("OK", fail))
				);
			}
		});
	}

	private void withLogin(final Callable success, final Callable fail) {
		if (authManager.getCurrentLogin() == null) {
			setScreen(new LoginSelection(this,
					new Callable() {
						@Override
						public Object call() {
							fail.call();
							return null;
						}
					},
					new Callable() {
						@Override
						public Object call() {
							loginAndDo(AuthManager.LoginMethod.Facebook, success, fail);
							return null;
						}
					},
					new Callable() {
						@Override
						public Object call() {
							loginAndDo(AuthManager.LoginMethod.Google, success, fail);
							return null;
						}
					},
					new Callable() {
						@Override
						public Object call() {
							loginAndDo(AuthManager.LoginMethod.Username, success, fail);
							return null;
						}
					}
			));

		} else {
			withRegistration(authManager, success, fail);
		}
	}

	private void multiPlayer() {
		withLogin(
				new Callable() {
					@Override
					public Object call() {
						setScreen(new MultiplayerLobby(
								TurboHearts.this,
								authManager.getCurrentLogin(),
								lobbyManager,
								new Callable() {
									@Override
									public Object call() {
										startMainMenu();
										return null;
									}
								}
						));
						return null;
					}
				},
				new Callable() {
					@Override
					public Object call() {
						Gdx.app.log("TurboHearts", "Fail callback called");
						startMainMenu();
						return null;
					}
				}
		);

	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		double aspectRatio = (double)width / height;
		camera.setToOrtho(false, (float)(480f * aspectRatio), 480);

		spriteBatch.setProjectionMatrix(camera.combined);
	}

	@Override
	public AssetManager getManager() {
		return manager;
	}

	@Override
	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	@Override
	public OrthographicCamera getCamera() {
		return camera;
	}

	// We need to maintain ourselves in the inputprocessor list
	// to support global back button
	@Override
	public void setInputProcessor(InputProcessor inputProcessor) {
		if (inputMultiplexer.size() == 2) {
			inputMultiplexer.removeProcessor(1);
		}
		if (inputProcessor != null) {
			inputMultiplexer.addProcessor(inputProcessor);
		}
	}

	@Override
	public void setScreen(TurboScreen screen) {
		Gdx.app.log("TurboHearts", "Switching screen: " + screen.getClass().getCanonicalName());
		currentTurboScreen = screen;
		setScreen((Screen) screen);
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.BACK && currentTurboScreen != null){
			currentTurboScreen.onBack();
			return true;
		}
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
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
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
}
