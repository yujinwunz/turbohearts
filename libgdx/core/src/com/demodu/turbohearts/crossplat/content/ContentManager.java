package com.demodu.turbohearts.crossplat.content;

import com.demodu.turbohearts.crossplat.auth.Avatar;

import java.util.List;

public interface ContentManager {

	void queryPublicLeaderBoard(com.demodu.turbohearts.crossplat.auth.Profile profile, LeaderboardResultCallback callback);

	interface LeaderboardResultCallback {
		void onLeaderboardLoaded(List<LeaderBoardEntry> leaderBoardEntries);
	}

	class LeaderBoardEntry {
		private Avatar avatar;
		private int elo;

		public LeaderBoardEntry(Avatar avatar, int elo) {

			this.avatar = avatar;
			this.elo = elo;
		}

		public Avatar getAvatar() {
			return avatar;
		}

		public int getElo() {
			return elo;
		}
	}
}
