package com.demodu.turbohearts.service;

import com.demodu.turbohearts.api.messages.AuthenticatedRequest;
import com.demodu.turbohearts.service.models.UserSession;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import org.hibernate.Session;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import static com.demodu.turbohearts.service.Global.verifier;

/**
 * Created by yujinwunz on 13/04/2017.
 */

public class AuthHelpers {
	public static Response withLoginAndDb(AuthenticatedRequest request, PrivilegedTask task) {
		try {
			GoogleIdToken idToken = verifier.verify(request.getAuthToken());
			if (idToken == null) {
				return Response.status(401).entity("Id token null").build();
			} else {

				Response result = null;
				Session session = Global.sessionFactory.openSession();
				session.beginTransaction();
				try {
					CriteriaBuilder builder = session.getCriteriaBuilder();
					CriteriaQuery<UserSession> criteriaQuery = builder.createQuery(UserSession.class);
					Expression idTokenExpr = criteriaQuery.from(UserSession.class).get("idToken");
					criteriaQuery.where(builder.equal(idTokenExpr, request.getAuthToken()));

					List<UserSession> results = session.createQuery(criteriaQuery).list();
					if (results.size() == 0) {
						result = Response.status(401).entity("invalid session").build();
					} else if (results.get(0).getUser() == null) {
						result = Response.status(401).entity("user not found").build();
					} else {
						result = task.run(results.get(0), session);
					}
					session.getTransaction().commit();
				} finally {
					session.close();
				}

				return result;
			}


		} catch (IOException ex) {
			return Response.status(500).entity("Cannot connect to OAuth Login").build();
		} catch (GeneralSecurityException ex) {
			return Response.status(401).entity("Unknown security error").build();
		}
	}

	public static void withLoginAndDbAsync(
			AuthenticatedRequest request,
			AsyncResponse response,
			AsyncPrivilegedTask asyncPrivilegedTask
	) {
		Response immediateResponse =
				withLoginAndDb(request, (UserSession userSession, Session session) -> {
					asyncPrivilegedTask.run(userSession, session);
					return null;
				});
		if (immediateResponse != null) {
			System.out.println("Async gave immediate response: " + immediateResponse + immediateResponse.getEntity());
			response.resume(immediateResponse);
		}
	}

	public interface PrivilegedTask {
		Response run(UserSession userSession, Session session);
	}

	public interface AsyncPrivilegedTask {
		void run(UserSession userSession, Session session);
	}
}
