package com.demodu.gwtcompat;

/* To help gwt compile quicker */
public interface Callable<V> {
	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	V call() throws Exception;
}