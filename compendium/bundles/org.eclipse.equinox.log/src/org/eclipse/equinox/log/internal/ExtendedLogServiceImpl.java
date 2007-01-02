/*******************************************************************************
 * Copyright (c) 2006, 2007 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *     Cognos Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.log.internal;

import org.eclipse.equinox.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class ExtendedLogServiceImpl implements LogService, Logger {

	private ExtendedLogServiceFactory factory;
	private Bundle bundle;

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

	public Logger getLogger(String name) {
		return factory.getLogger(bundle, name);
	}

	public String getName() {
		return getLogger(null).getName();
	}

	public boolean isLoggable(int level) {
		return getLogger(null).isLoggable(level);
	}
}
