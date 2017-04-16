package com.demodu.turbohearts.crossplat.content;

import com.demodu.turbohearts.crossplat.auth.Avatar;

import java.util.ArrayList;
import java.util.List;

public class ExampleContentManager implements ContentManager {
	@Override
	public void queryPublicLeaderBoard(com.demodu.turbohearts.crossplat.auth.Profile profile, LeaderboardResultCallback callback) {
		List<LeaderBoardEntry> leaderBoardEntries = new ArrayList<LeaderBoardEntry>();

		int uid = 0;
		for (int score = 2000; score > 1500; score -= 100) {
			uid += 1;
			leaderBoardEntries.add(new LeaderBoardEntry(
					new Avatar("Test user " + uid),
					score
			));
		}

		callback.onLeaderboardLoaded(leaderBoardEntries);
	}
}
