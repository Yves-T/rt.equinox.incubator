/*******************************************************************************
 * Copyright (c) 2006, 2008 Cognos Incorporated, IBM Corporation and others
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.equinox.log.internal;

import java.util.HashMap;
import java.util.WeakHashMap;
import org.eclipse.equinox.log.ExtendedLogService;
import org.eclipse.equinox.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class ExtendedLogServiceImpl implements ExtendedLogService {

	private final ExtendedLogServiceFactory factory;
	private final Bundle bundle;
	private HashMap loggerCache = new HashMap();
	private WeakHashMap bundleLogServices = null;

	public ExtendedLogServiceImpl(ExtendedLogServiceFactory factory, Bundle bundle) {
		this.factory = factory;
		this.bundle = bundle;
	}

	public void log(int level, String message) {
		log(null, level, message, null);
	}

	public void log(int level, String message, Throwable exception) {
		log(null, level, message, exception);
	}

	public void log(ServiceReference sr, int level, String message) {
		log(sr, level, message, null);
	}

	public void log(ServiceReference sr, int level, String message, Throwable exception) {
		getLogger(null).log(sr, level, message, exception);
	}

	public void log(Object context, int level, String message) {
		log(context, level, message, null);
	}

	public void log(Object context, int level, String message, Throwable exception) {
		getLogger(null).log(context, level, message, exception);
	}

	public synchronized Logger getLogger(String name) {
		checkShutdown();
		Logger logger = (Logger) loggerCache.get(name);
		if (logger == null) {
			logger = new LoggerImpl(this, name);
			loggerCache.put(name, logger);
		}
		return logger;
	}

	public Logger getLogger(Bundle logBundle, String name) {
		if (logBundle == null)
			throw new IllegalArgumentException("bundle cannot be null"); //$NON-NLS-1$

		if (logBundle == bundle)
			return getLogger(name);

		ExtendedLogService bundleLogService = getLogService(logBundle);
		return bundleLogService.getLogger(name);
	}

	private synchronized ExtendedLogService getLogService(Bundle logBundle) {
		factory.checkLogPermission();
		checkShutdown();
		if (bundleLogServices == null)
			bundleLogServices = new WeakHashMap();

		ExtendedLogService bundleLogService = (ExtendedLogService) bundleLogServices.get(logBundle);
		if (bundleLogService == null) {
			bundleLogService = new ExtendedLogServiceImpl(factory, logBundle);
			bundleLogServices.put(bundle, bundleLogService);
		}
		return bundleLogService;
	}

	public String getName() {
		return getLogger(null).getName();
	}

	public boolean isLoggable(int level) {
		return getLogger(null).isLoggable(level);
	}

	// package private methods called from Logger
	boolean isLoggable(String name, int level) {
		checkShutdown(); // Note: best effort
		return factory.isLoggable(bundle, name, level);
	}

	// package private methods called from Logger
	void log(String name, Object context, int level, String message, Throwable exception) {
		checkShutdown(); // Note: best effort
		factory.log(bundle, name, context, level, message, exception);
	}

	private synchronized void checkShutdown() {
		if (loggerCache == null)
			throw new IllegalStateException("LogService for " + bundle.getSymbolicName() + " (id=" + bundle.getBundleId() + ") is shutdown."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	synchronized void shutdown() {
		loggerCache = null;
	}
}
