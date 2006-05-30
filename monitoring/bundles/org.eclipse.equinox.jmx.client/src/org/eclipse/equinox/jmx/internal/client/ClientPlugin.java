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
package org.eclipse.equinox.jmx.internal.client;

import java.util.StringTokenizer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.jmx.common.JMXConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @since 1.0
 */
public class ClientPlugin extends AbstractUIPlugin {

	public static final String PI_NAMESPACE = "org.eclipse.equinox.jmx.client"; //$NON-NLS-1$
	public static final String PT_TRANSPORT = "transport"; //$NON-NLS-1$

	static final String CONNECTION_PREFERENCE_DELIMITER = ","; //$NON-NLS-1$
	static final String CONNECTION_PREFERENCE_NAME = "jmxserviceurl"; //$NON-NLS-1$
	static final String DELIM = ":"; //$NON-NLS-1$

	//The shared instance.
	private static ClientPlugin plugin;

	/**
	 * The constructor.
	 */
	public ClientPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static ClientPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PI_NAMESPACE, path);
	}

	/**
	 * Log to the <code>ServerPlugin</code>s log.
	 * 
	 * @param message The message to log.
	 * @param exception The exception to log.
	 * @param iStatusSeverity The <code>IStatus</code> severity level.
	 */
	public static void log(String message, Throwable exception, int iStatusSeverity) {
		getDefault().getLog().log(new Status(iStatusSeverity, PI_NAMESPACE, 0, message, exception));
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
		String message = exception.getMessage();
		if (message == null)
			message = "Exception occurred.";
		log(message, exception, IStatus.ERROR);
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

	public String[] getConnectionPreference() {
		return convertConnectionPreference();
	}

	private String[] convertConnectionPreference() {
		StringTokenizer st = new StringTokenizer(getPreferenceStore().getString(CONNECTION_PREFERENCE_NAME), CONNECTION_PREFERENCE_DELIMITER);
		String[] ret = new String[st.countTokens()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = st.nextToken();
		}
		return ret;
	}

	public String getDefaultConnectionPreference() {
		return getPreferenceStore().getDefaultString(CONNECTION_PREFERENCE_NAME);
	}

	public void setConnectionPreferences(String[] connections) {
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < connections.length; i++) {
			sbuf.append(connections[i]).append(CONNECTION_PREFERENCE_DELIMITER);
		}
		getPreferenceStore().setValue(CONNECTION_PREFERENCE_NAME, sbuf.toString());
	}

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(CONNECTION_PREFERENCE_NAME, "service:jmx:rmi:///jndi/rmi://127.0.0.1:8118/" + JMXConstants.DEFAULT_DOMAIN);
	}
}