package com.demodu.turbohearts.service.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Quick and dirty solution to the web-db-concurrency problem.
 */

public class ModelLocks {

	public static class LobbyRoom extends LockGroup {}
	public static class User extends LockGroup {}
	public static class UserSession extends LockGroup {}

	private abstract static class LockGroup {
		public static Map<Object, Object> lockCache = new HashMap<Object, Object>();

		public static synchronized Object getLock(Object obj) {
			Object lock = lockCache.getOrDefault(obj, new Object());
			lockCache.put(obj, lock);
			return lock;
		}
	}
}
