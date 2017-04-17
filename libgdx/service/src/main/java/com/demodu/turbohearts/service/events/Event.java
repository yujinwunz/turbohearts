package com.demodu.turbohearts.service.events;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Event {

	public static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonIgnore
	public abstract Class<? extends Event> getEventClass();
}
