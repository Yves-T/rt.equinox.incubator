/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.log.internal;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.equinox.log.ExtendedLogService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator {

	private static final String[] LOGSERVICE_CLASSES = {LogService.class.getName(), ExtendedLogService.class.getName()};
	private static final String[] LOGREADERSERVICE_CLASSES = {LogReaderService.class.getName(), ExtendedLogReaderService.class.getName()};
	
	private ServiceRegistration logReaderServiceRegistration;
	private ServiceRegistration logServiceRegistration;

	public void start(BundleContext context) throws Exception {
		ExtendedLogReaderServiceFactory logReaderServiceFactory = new ExtendedLogReaderServiceFactory();
		ExtendedLogServiceFactory logServiceFactory = new ExtendedLogServiceFactory(logReaderServiceFactory);
		
		logReaderServiceRegistration = context.registerService(LOGREADERSERVICE_CLASSES, logReaderServiceFactory, null);
		logServiceRegistration = context.registerService(LOGSERVICE_CLASSES, logServiceFactory, null);
	}

	public void stop(BundleContext context) throws Exception {
		logServiceRegistration.unregister();
		logServiceRegistration = null;
		logReaderServiceRegistration.unregister();
		logReaderServiceRegistration = null;
	}

}
