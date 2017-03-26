package com.demodu.crossplat.content;

import com.demodu.crossplat.auth.Avatar;
import com.demodu.crossplat.auth.Profile;

import java.util.List;

public interface ContentManager {

	List<LeaderBoardEntry> getPublicScoreBoard(Profile profile);



	class LeaderBoardEntry {
		Avatar avatar;
		int elo;
	}
}
