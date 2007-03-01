package org.eclipse.equinox.examples.splash;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.service.runnable.StartupMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.equinox.splash";

	// The shared instance
	private static Activator plugin;
	private BundleContext context = null;
	private static Splash splash;
	private static ServiceRegistration registration;
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
		super.start(context);
		plugin = this;
		this.context = context;
		
		splash = new Splash();
		Dictionary properties = new Hashtable();
		properties.put(Constants.SERVICE_RANKING, new Integer(-42));
		registration = context.registerService(StartupMonitor.class.getName(), splash, properties);
		context.addBundleListener( splash );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		registration.unregister();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public BundleContext getContext() {
		return context;
	}
}
