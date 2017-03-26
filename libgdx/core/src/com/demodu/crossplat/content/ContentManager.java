package com.demodu.crossplat.content;

import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;

import java.util.List;

public interface ContentManager {

	void queryPublicLeaderBoard(Profile profile, LeaderboardResultCallback callback);

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
