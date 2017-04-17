package com.demodu.turbohearts.gwtcompat;

/* To help gwt compile quicker */
public interface Callable<V> {
	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 */
	V call();
}
