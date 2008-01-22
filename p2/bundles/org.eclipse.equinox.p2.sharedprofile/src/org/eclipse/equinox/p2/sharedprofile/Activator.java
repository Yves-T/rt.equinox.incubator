package org.eclipse.equinox.p2.sharedprofile;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.equinox.p2.sharedprofile";
	private static BundleContext bundleContext;

	public static BundleContext getContext() {
		return bundleContext;
	}

	public void start(BundleContext context) throws Exception {
		Activator.bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		Activator.bundleContext = null;
	}

}
