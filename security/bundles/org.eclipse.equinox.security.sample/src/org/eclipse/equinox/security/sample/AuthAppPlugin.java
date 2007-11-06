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

import org.eclipse.osgi.internal.provisional.verifier.CertificateTrustAuthority;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class AuthAppPlugin implements BundleActivator {

	private static BundleContext bundleContext;

	private static ServiceTracker platformAdminTracker;
	private static ServiceTracker certTrustAuthorityTracker;
	private static ServiceTracker packageAdminTracker;

	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		bundleContext = context;

		if (null != platformAdminTracker)
			platformAdminTracker.close();

		if (null != certTrustAuthorityTracker)
			certTrustAuthorityTracker.close();

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

	public static CertificateTrustAuthority getCertTrustAuthority() {
		if (null == certTrustAuthorityTracker) {
			certTrustAuthorityTracker = new ServiceTracker(bundleContext, CertificateTrustAuthority.class.getName(), null);
			certTrustAuthorityTracker.open();
		}
		return (CertificateTrustAuthority) certTrustAuthorityTracker.getService();
	}

	public static PackageAdmin getPackageAdmin() {
		if (null == packageAdminTracker) {
			packageAdminTracker = new ServiceTracker(bundleContext, PackageAdmin.class.getName(), null);
			packageAdminTracker.open();
		}
		return (PackageAdmin) packageAdminTracker.getService();
	}
}
