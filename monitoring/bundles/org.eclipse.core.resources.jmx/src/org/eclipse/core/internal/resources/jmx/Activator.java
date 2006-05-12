/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.jmx;

import java.net.URL;
import org.eclipse.core.runtime.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @since 1.0
 */
public class Activator implements BundleActivator {

	private static final String BUNDLE_NAME = "org.eclipse.core.resources.jmx"; //$NON-NLS-1$
	private static BundleContext context;
	private static ServiceTracker logService;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (logService != null) {
			logService.close();
			logService = null;
		}
		context = null;
	}

	/*
	 * Log the given error.
	 */
	public static void log(String message, Exception e) {
		if (logService == null) {
			logService = new ServiceTracker(context, ILog.class.getName(), null);
			logService.open();
		}
		ILog log = (ILog) logService.getService();
		if (log == null)
			return;
		IStatus status = new Status(IStatus.ERROR, BUNDLE_NAME, 0, message, e);
		log.log(status);
	}

	static URL getImageLocation(String path) {
		return FileLocator.find(context.getBundle(), new Path(path), null);
	}

}
