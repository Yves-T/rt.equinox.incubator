/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.felix.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.equinox.frameworkadmin.FrameworkAdmin;
import org.osgi.framework.*;

/**
 * This bundle provides the tentative {@link FrameworkAdmin} implementation for Felix.
 * Essintially the bundle providing the {@link FrameworkAdmin} implementation for Felix
 * should be implemented by the implementator of Felix.
 * Therefore, this bundle is tantative.
 *
 */
public class Activator implements BundleActivator {
	static BundleContext context;

	private ServiceRegistration registrationFh;

	FelixFwAdminImpl fwAdmin = null;

	private void registerFrameworkAdmin() {
		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_VENDOR, "Eclipse.org");
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_FW_NAME, FelixConstants.FW_NAME);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_FW_VERSION, FelixConstants.FW_VERSION);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_NAME, FelixConstants.LAUNCHER_NAME);
		props.put(FrameworkAdmin.SERVICE_PROP_KEY_LAUNCHER_VERSION, FelixConstants.LAUNCHER_VERSION);

		if (!FelixFwAdminImpl.isRunningFw(context)) {
			props.put(FrameworkAdmin.SERVICE_PROP_KEY_RUNNING_SYSTEM_FLAG, "true");
			fwAdmin = new FelixFwAdminImpl(context, true);
		} else
			fwAdmin = new FelixFwAdminImpl(context);

		fwAdmin = new FelixFwAdminImpl(context);
		registrationFh = context.registerService(FrameworkAdmin.class.getName(), fwAdmin, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		Log.init(context);
		registerFrameworkAdmin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
		if (registrationFh != null)
			registrationFh.unregister();
		if (fwAdmin != null)
			fwAdmin.deactivate();
	}

}
