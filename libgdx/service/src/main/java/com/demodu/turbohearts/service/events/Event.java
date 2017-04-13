package com.demodu.turbohearts.service.events;


public abstract class Event {
	public abstract Class<? extends Event> getEventClass();
}
