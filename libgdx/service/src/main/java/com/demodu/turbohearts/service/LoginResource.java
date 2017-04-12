package com.demodu.turbohearts.service;

import com.demodu.turbohearts.service.api.Global;
import com.demodu.turbohearts.service.api.messages.ImmutableLoginResponse;
import com.demodu.turbohearts.service.api.messages.LoginRequest;
import com.demodu.turbohearts.service.models.User;
import com.demodu.turbohearts.service.models.UserSession;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import org.hibernate.Session;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
				Session session = Global.sessionFactory.openSession();
				User user = getOrCreateSession(
						request.getIdToken(),
						idToken.getPayload().getSubject(),
						session
				).getUser();

				session.getTransaction().commit();
				session.close();

				if (user.getId() != idToken.getPayload().getSubject()) {
					throw new GeneralSecurityException(
								"OAuth Error: User ID does not correspond to the idToken"
					);
				}

				return Response.ok(ImmutableLoginResponse
						.builder()
						.success(true)
						.username(user.getUsername())
						.displayName(user.getDisplayName())
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

	private UserSession getOrCreateSession(String idToken, String userId, Session session) {
		User user = getOrCreateUser(userId, session);
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

		CriteriaQuery<UserSession> query = criteriaBuilder.createQuery(UserSession.class);
		query.where(criteriaBuilder.equal(query.from(UserSession.class).get("idToken"), idToken));
		List<UserSession> list = session.createQuery(query).list();

		if (list.size() == 1) {
			return list.get(0);
		} else {
			UserSession newSession = new UserSession(idToken, user);
			session.save(newSession);
			return newSession;
		}
	}

	private User getOrCreateUser(String id, Session session) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

		CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
		Root userRoot = query.from(User.class);
		query.where(criteriaBuilder.equal(userRoot.get("id"), id));

		List<User> list = session.createQuery(query).list();
		if (list.size() == 1) {
			return list.get(0);
		}

		User user = new User(id, null, null);
		session.save(user);
		return user;
	}

	@GET
	@Path("hello")
	@Produces(MediaType.TEXT_PLAIN)
	public String helloWorld() {
		return "Hello World";
	}

}
