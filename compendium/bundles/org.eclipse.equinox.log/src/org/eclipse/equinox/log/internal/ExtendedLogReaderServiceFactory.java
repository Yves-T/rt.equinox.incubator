/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.log.internal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.equinox.log.LogFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

public class ExtendedLogReaderServiceFactory implements ServiceFactory {

	private Map listeners = new HashMap();

	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return new LogReaderServiceImpl(this);
	}

	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
		LogReaderServiceImpl readerService = (LogReaderServiceImpl) service;
		readerService.shutdown();
	}

	public boolean isLoggable(Bundle bundle, String name, int level) {		
		for (Iterator it = listeners.values().iterator(); it.hasNext();) {
			LogFilter filter = (LogFilter) it.next();
			if (filter.isLoggable(bundle, name, level))
				return true;
		}		
		return false;		
	}

	public void log(Bundle bundle, String name, Object context, int level, String message, Throwable exception) {
		LogEntry logEntry = new ExtendedLogEntryImpl(bundle, name, context, level, message, exception);
		for (Iterator it = listeners.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			LogFilter filter = (LogFilter) entry.getValue();
			if (filter.isLoggable(bundle, name, level)) {
				LogListener listener = (LogListener) entry.getKey();
				// should be sent asych
				listener.logged(logEntry);
			}
		}				
	}

	public void addLogListener(LogListener listener, LogFilter filter) {
		listeners.put(listener, filter);
	}

	public void removeLogListener(LogListener listener) {
		listeners.remove(listener);
	}

	public Enumeration getLog() {
		return new Enumeration() {
			public boolean hasMoreElements() {
				return false;
			}

			public Object nextElement() {
				return null;
			}
		};
	}
}
