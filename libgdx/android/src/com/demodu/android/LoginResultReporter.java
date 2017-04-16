package com.demodu.android;

import com.demodu.turbohearts.gwtcompat.Callable;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

/**
 * Created by yujinwunz on 12/04/2017.
 */

public interface LoginResultReporter {
	void report(GoogleSignInResult result, Callable onFinish);
}
