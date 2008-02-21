/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.internal.transforms.sed;

import java.util.Properties;

import org.eclipse.equinox.internal.transforms.sed.provisional.SEDTransformer;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private ServiceRegistration registration;

	private ServiceTracker urlConverterServiceTracker;
	private ServiceTracker logTracker;

	public void start(BundleContext context) throws Exception {
		logTracker = new ServiceTracker(context, FrameworkLog.class.getName(), null);
		logTracker.open();
		if (!SEDTransformer.isSedAvailable()) {
			log(FrameworkEvent.WARNING, "Sed application cannot be launched.  Sed transforms have been disabled.", null);
			return;
		}

		Filter filter;

		try {
			filter = context.createFilter("(objectClass=" + URLConverter.class.getName() + ")");
			urlConverterServiceTracker = new ServiceTracker(context, filter, null);
			urlConverterServiceTracker.open();
		} catch (InvalidSyntaxException e1) {
			log(FrameworkEvent.ERROR, "Cannot aquire URLConverter service.", e1);
			return;
		}

		Properties properties = new Properties();
		properties.put("isStreamTransformer", "true");

		Object transformer = new SEDTransformer(urlConverterServiceTracker, logTracker);
		registration = context.registerService(Object.class.getName(), transformer, properties);

	}

	void log(int severity, String msg, Throwable t) {
		FrameworkLog log = (FrameworkLog) logTracker.getService();
		if (log == null) {
			if (msg != null)
				System.err.println(msg);
			if (t != null)
				t.printStackTrace();
			return;
		}

		FrameworkLogEntry entry = new FrameworkLogEntry("org.eclipse.equinox.transforms.xslt", severity, 0, msg, 0, t, null);
		log.log(entry);
	}

	public void stop(BundleContext context) throws Exception {
		if (registration != null)
			registration.unregister();
		if (urlConverterServiceTracker != null)
			urlConverterServiceTracker.close();
		logTracker.close();
		context = null;
	}
}
