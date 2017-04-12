package com.demodu.turbohearts.api.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ApiMessage {

	public static final ObjectMapper objectMapper = new ObjectMapper();

	public String toJsonString() throws JsonProcessingException {
		return objectMapper.writeValueAsString(this);
	}
}
