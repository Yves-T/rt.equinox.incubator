/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.security.sample;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.provider.IProviderHints;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.security.TrustEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class AuthAppPlugin implements BundleActivator {

	/**
	 * The unique identifier constant of this plug-in.
	 */
	public static final String BUNDLE_ID = "org.eclipse.equinox.security.sample"; //$NON-NLS-1$
	private static BundleContext bundleContext;

	private static ServiceTracker platformAdminTracker;
	private static ServiceTracker packageAdminTracker;
	private static ServiceTracker engineTracker;

	/* stuff for the login configuration */
	private static final String CONFIG_PREF = "loginConfiguration"; //$NON-NLS-1$
	private static final String CONFIG_DEFAULT = "other"; //$NON-NLS-1$

	/* stuff for secure preferences */
	private static final String SECURE_PREFS_PROVIDER_ID = "org.eclipse.equinox.security.sample.SubjectPasswordProvider"; //$NON-NLS-1$
	private static final String SECURE_PREFS_FILENAME = "secureprefs.properties"; //$NON-NLS-1$
	private static File securePreferencesFile;
	private static ISecurePreferences securePreferences;

	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		securePreferencesFile = AuthAppPlugin.getBundleContext().getDataFile(SECURE_PREFS_FILENAME);
	}

	public void stop(BundleContext context) throws Exception {
		bundleContext = context;

		if (null != platformAdminTracker)
			platformAdminTracker.close();

		if (null != packageAdminTracker)
			packageAdminTracker.close();
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	public static PlatformAdmin getPlatformAdmin() {
		if (null == platformAdminTracker) {
			platformAdminTracker = new ServiceTracker(bundleContext, PlatformAdmin.class.getName(), null);
			platformAdminTracker.open();
		}
		return (PlatformAdmin) platformAdminTracker.getService();
	}

	public static PackageAdmin getPackageAdmin() {
		if (null == packageAdminTracker) {
			packageAdminTracker = new ServiceTracker(bundleContext, PackageAdmin.class.getName(), null);
			packageAdminTracker.open();
		}
		return (PackageAdmin) packageAdminTracker.getService();
	}

	public static TrustEngine[] getTrustEngines() {
		if (null == engineTracker) {
			engineTracker = new ServiceTracker(bundleContext, TrustEngine.class.getName(), null);
			engineTracker.open();
		}
		Object objs[] = engineTracker.getServices();
		TrustEngine[] result = new TrustEngine[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length);
		return result;
	}

	public static ISecurePreferences getSecurePreferences() {
		if (null == securePreferences) {
			Map options = new HashMap();
			options.put(IProviderHints.REQUIRED_MODULE_ID, SECURE_PREFS_PROVIDER_ID);
			try {
				securePreferences = SecurePreferencesFactory.open(securePreferencesFile.toURL(), options).node(BUNDLE_ID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return securePreferences;
	}

	public static String getConfigurationName() {
		return new DefaultScope().getNode(BUNDLE_ID).get(CONFIG_PREF, CONFIG_DEFAULT);
	}
}
