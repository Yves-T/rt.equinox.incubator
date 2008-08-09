/*******************************************************************************
 * Copyright (c) 2006, 2008 Cognos Incorporated, IBM Corporation and others
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.equinox.log.internal;

import org.osgi.framework.*;

public class ExtendedLogServiceFactory implements ServiceFactory {
	private final ExtendedLogReaderServiceFactory logReaderServiceFactory;

	public ExtendedLogServiceFactory(ExtendedLogReaderServiceFactory logReaderServiceFactory) {
		this.logReaderServiceFactory = logReaderServiceFactory;
	}

	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return new ExtendedLogServiceImpl(this, bundle);
	}

	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
		((ExtendedLogServiceImpl) service).shutdown();
	}

	public boolean isLoggable(Bundle bundle, String name, int level) {
		return logReaderServiceFactory.isLoggable(bundle, name, level);
	}

	public void log(Bundle bundle, String name, Object context, int level, String message, Throwable exception) {
		logReaderServiceFactory.log(bundle, name, context, level, message, exception);
	}
}
