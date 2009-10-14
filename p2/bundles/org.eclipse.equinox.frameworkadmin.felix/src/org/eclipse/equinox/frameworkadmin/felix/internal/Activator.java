/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.felix.internal;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.osgi.framework.*;

/**
 * This bundle provides the tentative {@link FrameworkAdmin} implementation for Felix.
 * Optimally, the implementor of Felix would implement this bundle.
 *
 */
public class Activator implements BundleActivator {

	private static BundleContext bundleContext;
	private ServiceRegistration registrationFh;
	FelixFwAdminImpl fwAdmin = null;

	/*
	 * Return the bundle context.
	 */
	public static BundleContext getContext() {
		return bundleContext;
	}

	/*
	 * Register our framework admin implementation as a service.
	 */
	private void registerFrameworkAdmin() {
		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_VENDOR, "Eclipse.org"); //$NON-NLS-1$
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_FW_NAME, FelixConstants.FW_NAME);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_FW_VERSION, FelixConstants.FW_VERSION);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_NAME, FelixConstants.LAUNCHER_NAME);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_VERSION, FelixConstants.LAUNCHER_VERSION);

		if (FelixFwAdminImpl.isRunningFw(bundleContext)) {
			props.put(FrameworkAdmin.SERVICE_PROP_KEY_RUNNING_SYSTEM_FLAG, "true"); //$NON-NLS-1$
			fwAdmin = new FelixFwAdminImpl(bundleContext, true);
		} else
			fwAdmin = new FelixFwAdminImpl(bundleContext);

		fwAdmin = new FelixFwAdminImpl(bundleContext);
		registrationFh = bundleContext.registerService(FrameworkAdmin.class.getName(), fwAdmin, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.bundleContext = context;
		registerFrameworkAdmin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Activator.bundleContext = null;
		if (registrationFh != null)
			registrationFh.unregister();
		if (fwAdmin != null)
			fwAdmin.deactivate();
	}

	/*
	 * Delete the given file whether it is a file or a directory
	 */
	public static void deleteAll(File file) {
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null)
				for (int i = 0; i < files.length; i++)
					deleteAll(files[i]);
		}
		file.delete();
	}

}
