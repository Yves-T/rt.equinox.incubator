package org.eclipse.equinox.p2.diagnostic;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentInstance;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	private static ComponentInstance componentInstance;
	private static BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
	}

	public static BundleContext getContext() {
		return context;
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}

	public static void setComponentInstance(ComponentInstance componentInstance) {
		Activator.componentInstance = componentInstance;
	}

	public static ComponentInstance getComponentInstance() {
		return componentInstance;
	}
}
