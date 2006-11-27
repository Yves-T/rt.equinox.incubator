/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.log.internal;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.equinox.log.ExtendedLogEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class ExtendedLogEntryImpl implements ExtendedLogEntry {

	private static int nextThreadID = 1;
	private static long nextSequenceNumber = 1;
	private static Map threadIDs = new WeakHashMap();

	private String loggerName;
	private Bundle bundle;
	private int level;
	private String message;
	private Throwable throwable;
	private Object contextObject;
	private long time;
	private long threadID;
	private long sequenceNumber;

	private static synchronized int getID(Thread thread) {
		Integer threadID = (Integer) threadIDs.get(thread);
		if (threadID == null) {
			threadID = new Integer(nextThreadID++);
			threadIDs.put(thread, threadID);
		}
		return threadID.intValue();
	}
		public ExtendedLogEntryImpl(Bundle bundle, String loggerName, Object contextObject, int level, String message, Throwable throwable) {
		this.loggerName = loggerName;
		this.bundle = bundle;
		this.level = level;
		this.message = message;
		this.throwable = throwable;
		this.contextObject = contextObject;

		synchronized (ExtendedLogEntryImpl.class) {
			this.sequenceNumber = nextSequenceNumber++;
			this.threadID = getID(Thread.currentThread());
		}
	}

	public String getLoggerName() {
		return loggerName;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public long getThreadID() {
		return threadID;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public Throwable getException() {
		return throwable;
	}

	public int getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public ServiceReference getServiceReference() {
		if (contextObject != null && contextObject instanceof ServiceReference)
			return (ServiceReference) contextObject;

		return null;
	}

	public long getTime() {
		return time;
	}

	public Object getContext() {
		return contextObject;
	}
}
