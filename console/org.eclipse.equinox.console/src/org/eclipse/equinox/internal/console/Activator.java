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
package org.eclipse.equinox.internal.console;

import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	volatile private static ServiceTracker startLevelManagerTracker;
	volatile private static ServiceTracker condPermAdminTracker;
	volatile private static ServiceTracker permissionAdminTracker;
	volatile private static ServiceTracker packageAdminTracker;
	volatile private static ServiceTracker platformAdminTracker;

	private static final String CONSOLE_NAME = "OSGi Console"; //$NON-NLS-1$
	private FrameworkConsole console;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		int port = 0;
		String portString = context.getProperty("osgi.console"); //$NON-NLS-1$
		if (portString == null)
			return; // do nothing
		port = portString.length() > 0 ? Integer.parseInt(portString) : 0;
		// grab conditional permission admin
		condPermAdminTracker = new ServiceTracker(context, ConditionalPermissionAdmin.class.getName(), null);
		condPermAdminTracker.open();

		// grab permission admin
		permissionAdminTracker = new ServiceTracker(context, PermissionAdmin.class.getName(), null);
		permissionAdminTracker.open();

		startLevelManagerTracker = new ServiceTracker(context, StartLevel.class.getName(), null);
		startLevelManagerTracker.open();

		packageAdminTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
		packageAdminTracker.open();

		platformAdminTracker = new ServiceTracker(context, PlatformAdmin.class.getName(), null);
		platformAdminTracker.open();

		console = new FrameworkConsole(context, port);
		Thread t = new Thread(console, CONSOLE_NAME);
		t.setDaemon(false);
		t.start();
	}

	public static StartLevel getStartLevel() {
		return (StartLevel) getServiceFromTracker(startLevelManagerTracker, StartLevel.class.getName());
	}

	public static PermissionAdmin getPermissionAdmin() {
		return (PermissionAdmin) getServiceFromTracker(permissionAdminTracker, PermissionAdmin.class.getName());
	}

	public static ConditionalPermissionAdmin getConditionalPermissionAdmin() {
		return (ConditionalPermissionAdmin) getServiceFromTracker(condPermAdminTracker, ConditionalPermissionAdmin.class.getName());
	}

	public static PackageAdmin getPackageAdmin() {
		return (PackageAdmin) getServiceFromTracker(packageAdminTracker, PackageAdmin.class.getName());
	}

	public static PlatformAdmin getPlatformAdmin() {
		return (PlatformAdmin) getServiceFromTracker(platformAdminTracker, PlatformAdmin.class.getName());
	}

	private static Object getServiceFromTracker(ServiceTracker tracker, String serviceClass) {
		if (tracker == null)
			throw new IllegalStateException("Missing service: " + serviceClass);
		Object result = tracker.getService();
		if (result == null)
			throw new IllegalStateException("Missing service: " + serviceClass);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		console.shutdown();
		console = null;

		if (startLevelManagerTracker != null)
			startLevelManagerTracker.close();
		startLevelManagerTracker = null;

		if (permissionAdminTracker != null)
			permissionAdminTracker.close();
		permissionAdminTracker = null;

		if (condPermAdminTracker != null)
			condPermAdminTracker.close();
		condPermAdminTracker = null;

		if (packageAdminTracker != null)
			packageAdminTracker.close();
		packageAdminTracker = null;

		if (platformAdminTracker != null)
			platformAdminTracker.close();
		platformAdminTracker = null;
	}

}
