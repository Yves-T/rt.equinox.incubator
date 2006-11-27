/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.log.internal;

import org.eclipse.equinox.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class LoggerImpl implements Logger {

	private ExtendedLogServiceFactory factory;
	private Bundle bundle; 
	private String name;

	public LoggerImpl(ExtendedLogServiceFactory factory, Bundle bundle, String name) {
		this.factory = factory;
		this.bundle = bundle;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isLoggable(int level) {
		return factory.isLoggable(bundle, name, level);
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
		log(sr, level, message, exception);
	}

	public void log(Object context, int level, String message) {
		log(context, level, message, null);
	}
	public void log(Object context, int level, String message, Throwable exception) {
		factory.log(bundle, name, context, level, message, exception);
	}

}
