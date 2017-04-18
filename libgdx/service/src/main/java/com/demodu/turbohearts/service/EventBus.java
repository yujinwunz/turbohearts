package com.demodu.turbohearts.service;

import com.demodu.turbohearts.service.events.Event;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.glassfish.jersey.internal.util.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus {
	private BlockingQueue<Runnable> pipeline = new BlockingArrayQueue<>();
	private final Map<Class<? extends Event>, List<HandlerWrapper>> handlerMapping = new HashMap<>();
	private Logger logger;

	public EventBus(Logger logger) {
		this.logger = logger;
	}

	public <T extends Event, TH extends T> void subscribe(Class<T> eventType, Handler<TH> handler) {
		synchronized (handlerMapping) {
			List<HandlerWrapper> handlers = handlerMapping.getOrDefault(eventType, new ArrayList<HandlerWrapper>());
			handlers.add(new HandlerWrapper<>(handler));
			handlerMapping.put(eventType, handlers);
		}
	}

	public void fireEvent(Event event) {
		if (logger != null) {
			try {
				logger.log(Level.INFO, "Got event: " + Event.objectMapper.writeValueAsString(event));
			} catch (JsonProcessingException ex) {
				logger.log(Level.INFO, ("Couldn't write event as Json: " + event));
			}
		}
		synchronized (handlerMapping) {
			List<HandlerWrapper> handlers = handlerMapping.getOrDefault(
				event.getEventClass(),
				Collections.emptyList()
			);
			logger.log(Level.INFO, "The event has " + handlers.size() + " handlers.");
			for (HandlerWrapper h : handlers) {
					Runnable runnable = () -> {
					if (h.handleWrapped(event) == false) {
						synchronized (handlerMapping) {
							handlers.remove(h);
						}
					}
				};
				if (!pipeline.offer(runnable)) {
					logger.log(Level.SEVERE, "Event pipeline has been filled up.");
					throw new IllegalStateException("Event pipeline has been filled up");
				}
			}
		}
	}

	public Thread startWorkerThread() {
		Thread thread = new Thread(() -> {
			//noinspection InfiniteLoopStatement
			while (true) {
				try {
					Runnable r = pipeline.take();
					r.run();
				} catch (InterruptedException ex) {
					logger.log(Level.SEVERE, ("InterruptedException while waiting for event."));
					logger.log(Level.SEVERE, ExceptionUtils.exceptionStackTraceAsString(ex));
				} catch (Exception ex) {
					logger.log(Level.SEVERE, ("Event Bus Worker thread error: " + ex.getMessage()));
					logger.log(Level.SEVERE, ExceptionUtils.exceptionStackTraceAsString(ex));
				}
			}
		});
		thread.start();
		return thread;
	}

	public interface Handler<T extends Event> {
		// Return true to remain subscribed.
		boolean handle(T event);
	}

	private class HandlerWrapper<T extends Event> {
		private Handler<T> innerHandler;
		private boolean hasCancelled;

		HandlerWrapper(Handler<T> handler) {
			this.innerHandler = handler;
		}

		boolean handleWrapped(T event) {
			synchronized (this) { // Ensures that events given to a single handler are serial.
				if (!hasCancelled) {
					hasCancelled = !innerHandler.handle(event);
					return !hasCancelled;
				} else {
					return false;
				}
			}
		}
	}
}
