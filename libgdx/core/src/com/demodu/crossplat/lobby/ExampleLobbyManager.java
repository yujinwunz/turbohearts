package com.demodu.crossplat.lobby;

import com.badlogic.gdx.utils.Timer;
import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;
import com.demodu.gamelogic.LocalGameConductor;
import com.demodu.player.RandomAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by yujinwunz on 26/03/2017.
 */

public class ExampleLobbyManager implements LobbyManager {

	List<LobbyEntry> lobbyEntryList = new ArrayList<LobbyEntry>();
	Random random = new Random();
	State state = State.NotMatching;
	LobbyRoom lobbyRoom;
	Profile profile;

	@Override
	public void enterLobby(Profile profile, final LobbyListener listener) {
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
		if (state == State.InLobby) {
			int randId = Math.abs(random.nextInt());
			List<Avatar> avatars = new ArrayList<Avatar>();
			int numPlayers = Math.abs(random.nextInt()) % 4 + 1;
			for (int i = 0; i < numPlayers; i++) {
				avatars.add(new Avatar("Opponent #" + Math.abs(random.nextInt())));
			}
			lobbyEntryList.add(
					new LobbyEntry(
							new MatchId(Integer.toString(randId)),
							"Game #" + randId,
							avatars
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
		state = State.NotMatching;
	}

	@Override
	public void enterRoom(final LobbyEntry entry, final LobbyRoomListener lobbyRoomListener) {
		if (state != State.InRoom) {
			state = State.InRoom;
			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					if (state == State.InRoom) {
						lobbyRoom = new LobbyRoom(
								entry.getId(),
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
		if (state == State.InRoom) {
			int rand = Math.abs(random.nextInt()) % 10;
			if (rand == 10) {
				// add player
				if (lobbyRoom.getPlayers().size() < 4) {
					List<Avatar> newPlayers = lobbyRoom.getPlayers();
					newPlayers.add(new Avatar("Added player " + Math.abs(random.nextInt())));
					lobbyRoom = new LobbyRoom(
							lobbyRoom.getId(),
							newPlayers,
							newPlayers.get(0)
					);
					lobbyRoomListener.onPlayerListUpdate(lobbyRoom.getPlayers());
				}
			} else if (rand == 9) {
				// remove player
				if (lobbyRoom.getPlayers().size() > 1) {
					List<Avatar> newPlayers = lobbyRoom.getPlayers();
					newPlayers.remove(1 + Math.abs(random.nextInt()) % (newPlayers.size()-1));
					lobbyRoom = new LobbyRoom(
							lobbyRoom.getId(),
							newPlayers,
							newPlayers.get(0)
					);
					lobbyRoomListener.onPlayerListUpdate(lobbyRoom.getPlayers());
				}
			} else if (rand == 8) {
				// Randomly tries to start
				if (lobbyRoom.getPlayers().size() == 4 &&
						lobbyRoom.getOwner().equals(profile.getAvatar())) {
					startPlaying(lobbyRoomListener);
				}
			} else if (rand == 7) {
				// Randomly cancel
				if (lobbyRoom.getPlayers().size() == 4 &&
						lobbyRoom.getOwner().equals(profile.getAvatar())) {
					lobbyRoomListener.onCancel("The owner left the game.");
				}
			}

			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					runRoom(lobbyRoomListener);
				}
			}, 5.0f);
		}
	}

	private void startPlaying(LobbyRoomListener lobbyRoomListener) {
		List<Avatar> otherPlayers = lobbyRoom.getPlayers();
		Collections.shuffle(otherPlayers);
		otherPlayers.remove(profile.getAvatar());
		lobbyRoomListener.onPlay(
				new LocalGameConductor(
					new RandomAI(),
					new RandomAI(),
					new RandomAI()
				),
				new ExampleMatchManager(),
				lobbyRoom.getId(),
				otherPlayers.get(0),
				otherPlayers.get(1),
				otherPlayers.get(2)
		);
		state = State.NotMatching;
	}

	@Override
	public void exitRoom() {
		state = State.NotMatching;
		lobbyRoom = null;
	}

	@Override
	public void createRoom(final LobbyRoomListener lobbyRoomListener) {
		if (state == State.NotMatching) {
			state = State.InRoom;
			Timer.schedule(new Timer.Task() {
				@Override
				public void run() {
					if (state == State.InRoom) {
						int randId = Math.abs(random.nextInt());
						lobbyRoom = new LobbyRoom(
								new MatchId(Integer.toString(randId)),
								new ArrayList<Avatar>(Collections.singletonList(profile.getAvatar())),
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
	public void startGame(LobbyRoomListener lobbyRoomListener) {
		if (state == State.InRoom) {
			if (lobbyRoom.getOwner().equals(profile.getAvatar())) {
				startPlaying(lobbyRoomListener);
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
