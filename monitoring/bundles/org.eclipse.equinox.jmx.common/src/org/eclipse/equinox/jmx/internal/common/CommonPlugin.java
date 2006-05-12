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
package org.eclipse.equinox.jmx.internal.common;

import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CommonPlugin implements BundleActivator {

	// default server settings
	public static final String DEFAULT_DOMAIN = "jmxserver"; //$NON-NLS-1$
	public static final String DEFAULT_PORT = "3600"; //$NON-NLS-1$
	public static final int DEFAULT_PORT_AS_INT = 3600;
	public static final String DEFAULT_HOST_PORT = "127.0.0.1:" + DEFAULT_PORT; //$NON-NLS-1$

	public static final String PLUGIN_ID = "org.eclipse.equinox.jmx.common"; //$NON-NLS-1$

	// id constants of available extension points
	public static final String PT_CONTRIBUTIONUI = "contributionui"; //$NON-NLS-1$

	//The shared instance.
	private static CommonPlugin plugin;
	private static BundleContext bundleContext;

	/**
	 * The constructor.
	 */
	public CommonPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * @return The bundle context.
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Returns the shared instance.
	 */
	public static CommonPlugin getDefault() {
		return plugin;
	}

	/**
	 * Log to the <code>ServerPlugin</code>s log.
	 * 
	 * @param message The message to log.
	 * @param exception The exception to log.
	 * @param iStatusSeverity The <code>IStatus</code> severity level.
	 */
	public static void log(String message, Throwable exception, int iStatusSeverity) {
		//		getDefault().getLog().log(new Status(iStatusSeverity, PLUGIN_ID, 0, message, exception));
	}

	/**
	 * Logs the message to the <code>ServerPlugin</code>s log with
	 * status <code>IStatus.ERROR</code>.
	 * 
	 * @param message The message to log.
	 * @param exception The thrown exception.
	 */
	public static void logError(String message, Throwable exception) {
		log(message, exception, IStatus.ERROR);
	}

	/**
	 * Logs the message to the <code>ServerPlugin</code>s log with
	 * status <code>IStatus.ERROR</code>.
	 *
	 * @param exception The thrown exception.
	 */
	public static void logError(Throwable exception) {
		log(exception.getMessage(), exception, IStatus.ERROR);
	}

	/**
	 * Logs the message to the <code>ServerPlugin</code>s log with
	 * status <code>IStatus.INFO</code>.
	 * 
	 * @param message The message to log.
	 */
	public static void log(String message) {
		log(message, null, IStatus.INFO);
	}

	/**
	 * Logs the throwable to the <code>ServerPlugin</code>s log with
	 * status <code>IStatus.INFO</code>.
	 * 
	 * @param exception The thrown exception.
	 */
	public static void log(Throwable exception) {
		logError(exception.getMessage(), exception);
	}

}
