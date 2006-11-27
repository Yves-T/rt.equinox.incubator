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
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class ExtendedLogServiceFactory implements ServiceFactory {
	private ExtendedLogReaderServiceFactory logReaderServiceFactory;
	
	public ExtendedLogServiceFactory(ExtendedLogReaderServiceFactory logReaderServiceFactory) {
		this.logReaderServiceFactory = logReaderServiceFactory;
	}

	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return new ExtendedLogServiceImpl(this, bundle);
	}

	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
		// do nothing
	}

	public Logger getLogger(Bundle bundle, String name) {
		return new LoggerImpl(this, bundle, name);
	}

	public boolean isLoggable(Bundle bundle, String name, int level) {
		return logReaderServiceFactory.isLoggable(bundle, name, level);
	}

	public void log(Bundle bundle, String name, Object context, int level, String message, Throwable exception) {
		logReaderServiceFactory.log(bundle, name, context, level, message, exception);		
	}
}
