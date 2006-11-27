/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.log.internal;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.equinox.log.LogFilter;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogListener;

public class LogReaderServiceImpl implements ExtendedLogReaderService {

	private ExtendedLogReaderServiceFactory factory;
	private Set listeners = new HashSet();
	private static final LogFilter NULL_LOGGER_FILTER = new LogFilter() {
		public boolean isLoggable(Bundle b, String loggerName, int logLevel) {
			return (loggerName == null);
		}		
	};	

	public LogReaderServiceImpl(ExtendedLogReaderServiceFactory factory) {
		this.factory = factory;
	}

	public void addLogListener(LogListener listener, LogFilter filter) {
		listeners.add(listener);
		factory.addLogListener(listener, filter);
	}

	public synchronized void addLogListener(LogListener listener) {
		addLogListener(listener, NULL_LOGGER_FILTER);
	}

	public Enumeration getLog() {
		return factory.getLog();
	}

	public synchronized void removeLogListener(LogListener listener) {
		factory.removeLogListener(listener);
	}

	public synchronized void shutdown() {
		for(Iterator it = listeners.iterator(); it.hasNext();) {
			factory.removeLogListener((LogListener) it.next());
		}
		listeners.clear();
	}

}
