package com.demodu.turbohearts.service.api.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.WRAPPER_OBJECT)
public abstract class ApiMessage {

	public static final ObjectMapper objectMapper = new ObjectMapper();

}
