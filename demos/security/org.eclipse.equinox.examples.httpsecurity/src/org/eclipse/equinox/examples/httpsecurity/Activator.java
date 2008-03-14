package org.eclipse.equinox.examples.httpsecurity;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	volatile static Bundle bundle;

	public void start(BundleContext context) throws Exception {
		Activator.bundle = context.getBundle();
	}

	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
