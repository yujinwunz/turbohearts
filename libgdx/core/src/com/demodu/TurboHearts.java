package com.demodu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.demodu.assets.Assets;
import com.demodu.crossplat.auth.AuthManager;
import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.ExampleAuthManager;
import com.demodu.crossplat.auth.Profile;
import com.demodu.crossplat.content.ContentManager;
import com.demodu.crossplat.content.ExampleContentManager;
import com.demodu.crossplat.lobby.ExampleLobbyManager;
import com.demodu.crossplat.lobby.LobbyManager;
import com.demodu.gamelogic.LocalGameConductor;
import com.demodu.gwtcompat.Callable;
import com.demodu.player.RandomAI;
import com.demodu.screens.LoginSelection;
import com.demodu.screens.MainMenu;
import com.demodu.screens.Menu;
import com.demodu.screens.MultiplayerLobby;
import com.demodu.screens.RegisterUsername;
import com.demodu.turboheartsgame.TurboHeartsGame;

import java.util.Arrays;

public class TurboHearts extends Game implements GameContext {

	private AssetManager manager;
	private SpriteBatch spriteBatch;
	private OrthographicCamera camera;

	private AuthManager authManager;
	private ContentManager contentManager;
	private LobbyManager lobbyManager;

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
				new Avatar("Deep Red (Random AI)"),
				new Avatar("Groundnet (Random AI)"),
				new Avatar("Trapratus (Random AI)")));
	}

	private void withRegistration(final AuthManager authManager, final Callable success, final Callable fail) {
		if (authManager.getCurrentLogin().getUsername() == null) {
			setScreen(new RegisterUsername(TurboHearts.this, new RegisterUsername.RegisterCallback() {
				@Override
				public void onRegister(String username) {
					authManager.setUsername(username, new AuthManager.LoginCallback() {
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

	@Override
	public void setScreen(Screen screen) {
		Gdx.app.log("TurboHearts", "Switching screen: " + screen.getClass().getCanonicalName());
		super.setScreen(screen);
	}
}
