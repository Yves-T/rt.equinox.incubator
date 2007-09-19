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
import org.eclipse.equinox.internal.security.boot.MessageAccess;

public class ConfigurationProvider extends Configuration {

	private static Configuration target = null;

	public ConfigurationProvider() {
		// placeholder
	}

	public static void setTargetConfiguration(Configuration proxy) {
		target = proxy;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
	 */
	public synchronized AppConfigurationEntry[] getAppConfigurationEntry(String name) {
		if (target == null)
			throw new RuntimeException(MessageAccess.getString("configurationNotSet")); //$NON-NLS-1$
		return target.getAppConfigurationEntry(name);
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#refresh()
	 */
	public synchronized void refresh() {
		target.refresh();
	}

	public synchronized void setAppConfigurationEntry(String providerId, String name, AppConfigurationEntry[] entryList) {
		if (target instanceof ConfigurationProvider)
			((ConfigurationProvider) target).setAppConfigurationEntry(providerId, name, entryList);
		else
			throw new UnsupportedOperationException();
	}

	public synchronized String[] listAppConfigurationEntries(String providerId) {
		if (target instanceof ConfigurationProvider)
			return ((ConfigurationProvider) target).listAppConfigurationEntries(providerId);
		throw new UnsupportedOperationException();
	}

}
