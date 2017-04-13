package com.demodu.turbohearts.service;

import com.demodu.turbohearts.service.events.Event;

import org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by yujinwunz on 13/04/2017.
 */

public class EventBus {
	private BlockingQueue<Runnable> pipeline = new BlockingArrayQueue<>();
	private final Map<Class<? extends Event>, List<HandlerWrapper>> handlerMapping = new HashMap<>();

	public <T extends Event, TH extends T> void subscribe(Class<T> eventType, Handler<TH> handler) {
		synchronized (handlerMapping) {
			List<HandlerWrapper> handlers = handlerMapping.getOrDefault(eventType, new ArrayList<HandlerWrapper>());
			handlers.add(new HandlerWrapper<>(handler));
			handlerMapping.put(eventType, handlers);
		}
	}

	public void fireEvent(Event event) {

		synchronized (handlerMapping) {
			List<HandlerWrapper> handlers = handlerMapping.getOrDefault(
				event.getEventClass(),
				Collections.emptyList()
			);
			for (HandlerWrapper h : handlers) {
				Runnable runnable = () -> {
					if (h.handleWrapped(event) == false) {
						synchronized (handlerMapping) {
							handlers.remove(h);
						}
					}
				};
				if (!pipeline.offer(runnable)) {
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
					System.err.println("InterruptedException while waiting for event");
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
			synchronized (this) {
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
