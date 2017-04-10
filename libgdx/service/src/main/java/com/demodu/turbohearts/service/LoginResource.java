package com.demodu.turbohearts.service;

import com.demodu.turbohearts.service.api.messages.ImmutableLoginResponse;
import com.demodu.turbohearts.service.api.messages.LoginRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.demodu.turbohearts.service.api.Global.verifier;


@Path("/auth")
public class LoginResource {

	@POST
	@Path("login")
	@Consumes( { MediaType.APPLICATION_JSON })
	@Produces( { MediaType.APPLICATION_JSON })
	public Response login(LoginRequest request) {

		try {
			GoogleIdToken idToken = verifier.verify(request.getIdToken());


			if (idToken != null) {
				Payload payload = idToken.getPayload();

				// Look for existing users.

				return Response.ok(
						ImmutableLoginResponse
								.builder()
								.success(true)
								.build()
				).build();
			} else {
				return Response.ok(
						ImmutableLoginResponse
						.builder()
						.success(false)
						.errorMessage("Username or password incorrect")
						.build()
				).build();
			}
		} catch (IOException ex) {
			return Response.status(500).entity("Server could not complete login at this time.").build();
		} catch (GeneralSecurityException ex) {
			return Response.status(500).entity("Login attempt failed").build();
		}

	}



	@GET
	@Path("hello")
	@Produces(MediaType.TEXT_PLAIN)
	public String helloWorld() {
		return "Hello World";
	}

}
