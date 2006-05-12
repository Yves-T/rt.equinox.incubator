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
package org.eclipse.equinox.jmx.internal.server;

import java.util.*;
import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.jmx.common.*;
import org.eclipse.equinox.jmx.internal.server.ServerExtensionManager.ContributionExtensionDefinition;
import org.eclipse.equinox.jmx.server.ContributionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The plug-in startup class for the jmx server plug-in.
 * 
 * @since 1.0
 */
public class ServerPlugin implements BundleActivator {

	static final String PI_NAMESPACE = "org.eclipse.equinox.jmx.server"; //$NON-NLS-1$
	static final String PT_CONTRIBUTION = "contribution"; //$NON-NLS-1$
	static final String PT_PROVIDER = "provider"; //$NON-NLS-1$

	static final String PROTOCOL_PROPERTY_KEY = PI_NAMESPACE + ".protocol"; //$NON-NLS-1$
	static final String PORT_PROPERTY_KEY = PI_NAMESPACE + ".port"; //$NON-NLS-1$
	static final String DOMAIN_PROPERTY_KEY = PI_NAMESPACE + ".domain"; //$NON-NLS-1$

	//The shared instance.
	private static ServerPlugin plugin;
	private static BundleContext bundleContext;
	private static JMXConnectorServer jmxServer;
	private static RootContribution rootContribution;

	/**
	 * The constructor.
	 */
	public ServerPlugin() {
		plugin = this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		if (jmxServer == null) {
			createServer();
		}
		jmxServer.start();
	}

	/**
	 * Create the jmx server.  This should only be invoked once - the first time this plugin is started.
	 * 
	 * @throws Exception If an exception occurs when attempting to start the server.
	 */
	public void createServer() throws Exception {
		// determine which protocol to use
		String protocol = System.getProperty(PROTOCOL_PROPERTY_KEY);
		if (protocol == null) {
			protocol = JMXConstants.DEFAULT_PROTOCOL;
		}
		// determine port to listen on
		int port;
		String strPort = System.getProperty(PORT_PROPERTY_KEY);
		if (strPort == null) {
			port = JMXConstants.DEFAULT_PORT_AS_INT;
		} else {
			try {
				port = Integer.parseInt(strPort);
			} catch (NumberFormatException nfe) {
				log(nfe);
				port = JMXConstants.DEFAULT_PORT_AS_INT;
			}
		}
		String domain = System.getProperty(DOMAIN_PROPERTY_KEY);
		if (domain == null) {
			domain = JMXConstants.DEFAULT_DOMAIN;
		}
		jmxServer = JMXServerFactory.createJMXServer("127.0.0.1", port, protocol, JMXConstants.DEFAULT_DOMAIN, null);
		registerContributions();
	}

	private void registerContributions() throws IntrospectionException, ReflectionException, MBeanRegistrationException, NotCompliantMBeanException {
		final MBeanServer mbeanServer = jmxServer.getMBeanServer();
		try {
			mbeanServer.getMBeanInfo(RootContribution.OBJECT_NAME);
		} catch (Exception e) {
			//load extensions and add to contribution model
			Collection providers = ServerExtensionManager.getInstance().getContributionExtensionDefinitions();
			Iterator iter = providers.iterator();
			List proxiesToRegister = new ArrayList();
			while (iter.hasNext()) {
				ContributionExtensionDefinition defn = (ContributionExtensionDefinition) iter.next();
				ContributionProvider provider = defn.getContributionProvider();
				// register the providers with the mbean server
				provider.registerContribution(mbeanServer);
				if (defn.isRootProvider()) {
					proxiesToRegister.add(provider.createProxy());
				}
			}
			rootContribution = new RootContribution((ContributionProxy[]) proxiesToRegister.toArray(new ContributionProxy[proxiesToRegister.size()]));
			try {
				mbeanServer.registerMBean(rootContribution, RootContribution.OBJECT_NAME);
			} catch (Exception e1) {
				// should not occur since we previously checked for existence
			}
			ServerExtensionManager.getInstance().addObserver(new Observer() {
				public void update(Observable o, Object arg) {
					if (!(arg instanceof ContributionExtensionDefinition)) {
						return;
					}
					ContributionExtensionDefinition defn = (ContributionExtensionDefinition) arg;
					ContributionProvider rootProvider = defn.getContributionProvider();
					if (ServerExtensionManager.getInstance().getContributionExtensionDefinition(defn.getProviderClassName()) == null) {
						// root provider has been removed
						rootContribution.unregisterContributionProxy(rootProvider.createProxy());
						rootProvider.sendNotification(new Notification(ContributionNotificationEvent.NOTIFICATION_REMOVED, rootProvider, 0));
					} else {
						// new root provider installed or updated
						try {
							rootProvider.registerContribution(mbeanServer);
							rootContribution.registerContributionProxy(rootProvider.createProxy());
							rootContribution.sendNotification(new Notification(ContributionNotificationEvent.NOTIFICATION_UPDATED, rootContribution, 0));
						} catch (Exception e) {
							log(e);
						}
					}
				}
			});
		}
	}

	public static RootContribution getRootContribution() {
		return rootContribution;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		jmxServer.stop();
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
	public static ServerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Return the underlying <code>Server</code> which provides an interface to the jmx agent.
	 * 
	 * @return The server.
	 */
	public MBeanServer getServer() {
		return jmxServer.getMBeanServer();
	}

	/**
	 * Log to the <code>ServerPlugin</code>s log.
	 * 
	 * @param message The message to log.
	 * @param exception The exception to log.
	 * @param iStatusSeverity The <code>IStatus</code> severity level.
	 */
	public static void log(String message, Throwable exception, int iStatusSeverity) {
		// TODO fix this logging
		//		getDefault().getLog().log(new Status(iStatusSeverity, PI_NAMESPACE, 0, message, exception));
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
