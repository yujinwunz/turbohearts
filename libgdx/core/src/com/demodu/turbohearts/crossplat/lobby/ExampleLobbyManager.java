package com.demodu.turbohearts.crossplat.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.demodu.turbohearts.gamelogic.LocalGameConductor;
import com.demodu.turbohearts.game.player.RandomAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by yujinwunz on 26/03/2017.
 */

public class ExampleLobbyManager implements LobbyManager {

	List<LobbyRoom> lobbyEntryList = new ArrayList<LobbyRoom>();
	Random random = new Random();
	State state = State.NotMatching;
	LobbyRoom lobbyRoom;
	com.demodu.turbohearts.crossplat.auth.Profile profile;

	@Override
	public void enterLobby(com.demodu.turbohearts.crossplat.auth.Profile profile, final LobbyListener listener) {
		Gdx.app.log("ExampleLobbyManager", "entering lobby. State is " + state);
		if (profile == null) {
			throw new NullPointerException("Profile cannot be null");
		}
		this.profile = profile;

		if (state == State.InLobby) {
			return;
		}
		if (state == State.InRoom) {
			exitRoom();
		}
		state = State.InLobby;

		Timer.schedule(
				new Timer.Task() {
					@Override
					public void run() {
						updateLobby(listener);
					}
				},
				2.0f
		);
	}

	private void updateLobby(final LobbyListener listener) {
		Gdx.app.log("ExampleLobbyManager", "Lobby updating. State is " + state);
		if (state == State.InLobby) {
			int randId = Math.abs(random.nextInt());
			List<com.demodu.turbohearts.crossplat.auth.Avatar> avatars = new ArrayList<com.demodu.turbohearts.crossplat.auth.Avatar>();
			int numPlayers = Math.abs(random.nextInt()) % 4 + 1;
			for (int i = 0; i < numPlayers; i++) {
				avatars.add(new com.demodu.turbohearts.crossplat.auth.Avatar("Opponent #" + Math.abs(random.nextInt())));
			}
			lobbyEntryList.add(
					new LobbyRoom(
							randId,
							"Game #" + randId,
							avatars,
							avatars.get(0)
					)
			);

			listener.onLobbyList(lobbyEntryList);

			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					updateLobby(listener);
				}
			}, 5.0f);
		}
	}

	@Override
	public void exitLobby() {
		Gdx.app.log("ExampleLobbyManager", "Exiting lobby. State is " + state);
		if (state == State.InLobby) {
			state = State.NotMatching;
		}
	}

	@Override
	public void enterRoom(final LobbyRoom entry, final LobbyRoomListener lobbyRoomListener) {
		Gdx.app.log("ExampleLobbyManager", "entering room. State is " + state);
		if (state != State.InRoom) {
			state = State.InRoom;
			Gdx.app.log("ExampleLobbyManager", "trying to enter room");
			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					Gdx.app.log("ExampleLobbyManager", "enter room scheduled. State is " + state);
					if (state == State.InRoom) {
						Gdx.app.log("ExampleLobbyManager", "Calling onEnter");
						lobbyRoom = new LobbyRoom(
								entry.getId(),
								entry.getName(),
								entry.getPlayers(),
								entry.getPlayers().get(0)
						);
						lobbyRoomListener.onEnter(lobbyRoom);
						runRoom(lobbyRoomListener);
					}
				}
			}, 2.0f);
		}
	}

	private void runRoom(final LobbyRoomListener lobbyRoomListener) {
		Gdx.app.log("ExampleLobbyManager", "Running room. State is " + state);
		if (state == State.InRoom) {
			int rand = Math.abs(random.nextInt()) % 10;
			if (rand == 9 || rand == 3) {
				// add player
				if (lobbyRoom.getPlayers().size() < 4) {
					List<com.demodu.turbohearts.crossplat.auth.Avatar> newPlayers = lobbyRoom.getPlayers();
					newPlayers.add(new com.demodu.turbohearts.crossplat.auth.Avatar("Added player " + Math.abs(random.nextInt())));
					lobbyRoom = new LobbyRoom(
							lobbyRoom.getId(),
							lobbyRoom.getName(),
							newPlayers,
							newPlayers.get(0)
					);
					lobbyRoomListener.onPlayerListUpdate(lobbyRoom.getPlayers());
				}
			} else if (rand == 8) {
				// remove player
				if (lobbyRoom.getPlayers().size() > 1) {
					List<com.demodu.turbohearts.crossplat.auth.Avatar> newPlayers = lobbyRoom.getPlayers();
					newPlayers.remove(1 + Math.abs(random.nextInt()) % (newPlayers.size()-1));
					lobbyRoom = new LobbyRoom(
							lobbyRoom.getId(),
							lobbyRoom.getName(),
							newPlayers,
							newPlayers.get(0)
					);
					lobbyRoomListener.onPlayerListUpdate(lobbyRoom.getPlayers());
				}
			} else if (rand == 7) {
				// Randomly tries to start
				if (lobbyRoom.getPlayers().size() == 4 &&
						!lobbyRoom.getOwner().equals(profile.getAvatar())) {
					startPlaying(lobbyRoomListener);
				}
			} else if (rand == 6) {
				// Randomly cancel
				if (!lobbyRoom.getOwner().equals(profile.getAvatar())) {
					lobbyRoomListener.onCancel("The owner left the game.");
				}
			}

			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					runRoom(lobbyRoomListener);
				}
			}, 0.5f);
		}
	}

	private void startPlaying(LobbyRoomListener lobbyRoomListener) {
		Gdx.app.log("ExampleLobbyManager", "Starting to play. State is " + state);
		List<com.demodu.turbohearts.crossplat.auth.Avatar> otherPlayers = lobbyRoom.getPlayers();
		Collections.shuffle(otherPlayers);
		otherPlayers.remove(profile.getAvatar());
		lobbyRoomListener.onPlay(
				new LocalGameConductor(
					new RandomAI(),
					new RandomAI(),
					new RandomAI()
				),
				new ExampleMatchManager(),
				new MatchId(lobbyRoom.getId()),
				otherPlayers.get(0),
				otherPlayers.get(1),
				otherPlayers.get(2)
		);
		state = State.NotMatching;
	}

	@Override
	public void exitRoom() {
		Gdx.app.log("ExampleLobbyManager", "Exiting room. State is " + state);
		state = State.NotMatching;
		lobbyRoom = null;
	}

	private LobbyRoomListener creator;

	@Override
	public void createRoom(final RoomOptions options, final LobbyRoomListener lobbyRoomListener) {
		Gdx.app.log("ExampleLobbyManager", "Creating room. State is " + state);
		if (state != State.InRoom) {
			creator = lobbyRoomListener;
			state = State.InRoom;
			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					if (state == State.InRoom) {
						int randId = Math.abs(random.nextInt());
						lobbyRoom = new LobbyRoom(
								randId,
								options.getTitle(),
								new ArrayList<com.demodu.turbohearts.crossplat.auth.Avatar>(Collections.singletonList(profile.getAvatar())),
								profile.getAvatar()
						);
						lobbyRoomListener.onEnter(lobbyRoom);
						runRoom(lobbyRoomListener);
					}
				}
			}, 2.0f);
		}
	}

	@Override
	public void startGame() {
		Gdx.app.log("ExampleLobbyManager", "Starting game. State is " + state);
		if (state == State.InRoom) {
			if (lobbyRoom.getOwner().equals(profile.getAvatar())) {
				startPlaying(creator);
			} else {
				throw new UnsupportedOperationException("Can't start a game you don't own");
			}
		} else {
			throw new UnsupportedOperationException("Not in a game");
		}
	}

	private enum State {
		NotMatching, InLobby, InRoom
	}
}
