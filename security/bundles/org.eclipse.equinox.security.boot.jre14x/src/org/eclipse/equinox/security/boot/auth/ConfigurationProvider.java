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
package org.eclipse.equinox.security.boot.auth;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.eclipse.equinox.internal.security.boot.proxy.MessageAccess;

public class ConfigurationProvider extends Configuration {

	private static Configuration target = null;
	private static Object lock = new Object();

	public ConfigurationProvider() {
		// placeholder
	}

	public static void setTargetConfiguration(Configuration proxy) {
		synchronized (lock) {
			if (target != null)
				throw new RuntimeException(MessageAccess.getString("configurationAlreadySet")); //$NON-NLS-1$
			target = proxy;
		}
	}

	/**
	 * 
	 * @param proxy
	 * @throws SecurityException
	 */
	public static void unsetTargetConfiguration(Configuration proxy) {
		synchronized (lock) {
			if (proxy != target)
				throw new SecurityException(MessageAccess.getString("unsetConfigurationError")); //$NON-NLS-1$
			target = null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
		synchronized (lock) {
			if (target == null)
				throw new RuntimeException(MessageAccess.getString("configurationNotSet")); //$NON-NLS-1$
			return target.getAppConfigurationEntry(name);
		}
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#refresh()
	 */
	public void refresh() {
		synchronized (lock) {
			target.refresh();
		}
	}

	public void setAppConfigurationEntry(String providerId, String name, AppConfigurationEntry[] entryList) {
		synchronized (lock) {
			if (target instanceof ConfigurationProvider)
				((ConfigurationProvider) target).setAppConfigurationEntry(providerId, name, entryList);
			else
				throw new UnsupportedOperationException();
		}
	}

	public String[] listAppConfigurationEntries(String providerId) {
		synchronized (lock) {
			if (target instanceof ConfigurationProvider)
				return ((ConfigurationProvider) target).listAppConfigurationEntries(providerId);
			throw new UnsupportedOperationException();
		}
	}

}
