package com.demodu.turbohearts.service;

import com.demodu.turbohearts.service.events.Event;
import com.sun.javafx.tools.packager.Log;

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

public class EventPipeline {
	private BlockingQueue<Event> pipeline = new BlockingArrayQueue<>();
	private final Map<Event.Type, List<Handler>> handlerMapping = new HashMap<>();

	public void subscribe(Event.Type eventType, Handler handler) {
		synchronized (handlerMapping) {
			List<Handler> handlers = handlerMapping.getOrDefault(eventType, new ArrayList<Handler>());
			handlers.add(handler);
			handlerMapping.put(eventType, handlers);
		}
	}

	public void unsubscribe(Event.Type eventType, Handler handler) {
		synchronized (handlerMapping) {
			handlerMapping.get(eventType).remove(handler);
		}
	}

	public void fireEvent(Event event) {
		if (!pipeline.offer(event)) {
			throw new IllegalStateException("Event pipeline has been filled up");
		}
	}

	public Thread startWorkerThread() {
		Thread thread = new Thread(() -> {
			//noinspection InfiniteLoopStatement
			while (true) {
				try {
					Event event = pipeline.take();

					List<Handler> handlers;
					synchronized (handlerMapping) {
						handlers = new ArrayList<Handler>(
								handlerMapping.getOrDefault(
										event.getType(),
										Collections.emptyList()
								)
						);
					}
					for (Handler h : handlers) {
						h.handle(event);
					}

				} catch (InterruptedException ex) {
					Log.debug("InterruptedException while waiting for event");
				}
			}
		});
		thread.start();
		return thread;
	}

	public interface Handler {
		void handle(Event event);
	}
}
