package com.demodu.crossplat.content;

import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;

import java.util.ArrayList;
import java.util.List;

public class ExampleContentManager implements ContentManager {
	@Override
	public void queryPublicLeaderBoard(Profile profile, LeaderboardResultCallback callback) {
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
