package com.demodu.turbohearts.service;

import com.demodu.turbohearts.service.api.messages.ImmutableLoginResponse;
import com.demodu.turbohearts.service.api.messages.LoginRequest;
import com.demodu.turbohearts.service.api.messages.LoginResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/auth")
public class LoginResource {

	@POST
	@Path("login")
	@Consumes( { MediaType.APPLICATION_JSON })
	@Produces( { MediaType.APPLICATION_JSON })
	public LoginResponse login(LoginRequest request) {

		/**
		 * TODO: Login.
		 * 1. Check for existing token->user mapping
		 * 	2. Validate token
		 * 	3. Store mapping
		 * 4. return user
		 */
		return ImmutableLoginResponse.builder().build();
	}

	@GET
	@Path("hello")
	@Produces(MediaType.TEXT_PLAIN)
	public String helloWorld() {
		return "Hello World";
	}
}
