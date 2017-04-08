
package com.demodu.turbohearts.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Gateway extends AbstractHandler
{
	@Override
	public void handle( String target,
						final Request baseRequest,
						HttpServletRequest request,
						final HttpServletResponse response ) throws IOException,
			ServletException
	{

		final AsyncContext context = request.startAsync();
		System.err.println("Adding job...");
		synchronized (jobqueue) {
			jobqueue.add(new Callable() {
				@Override
				public Object call() throws Exception {

					// Declare response encoding and types
					response.setContentType("text/html; charset=utf-8");

					// Declare response status code
					response.setStatus(HttpServletResponse.SC_OK);

					// Write back response
					response.getWriter().println("<h1>Hello World</h1>");

					// Inform jetty that this request has now been handled
					baseRequest.setHandled(true);
					context.complete();
					return null;
				}
			});
			jobqueue.notifyAll();
			System.err.println("Added job");
		}
	}

	public static void main( String[] args ) throws Exception
	{
		Server server = new Server(8080);
		server.setHandler(new Gateway());

		startLongPolling();

		server.start();
		server.join();
	}

	private static List<Callable> jobqueue = new ArrayList<Callable>();

	private static void startLongPolling() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (jobqueue) {
					try {
						while (true) {
							while (jobqueue.size() == 0) {
								jobqueue.wait();
								System.err.println("Got monitor for jobqueue, size is now " + jobqueue.size());
							}

							System.err.println("Calling");
							jobqueue.remove(0).call();
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new UnknownError();
					}
				}
			}
		});
		t.start();
	}
}