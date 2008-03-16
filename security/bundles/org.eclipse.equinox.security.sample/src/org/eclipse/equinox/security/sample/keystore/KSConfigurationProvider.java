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
package org.eclipse.equinox.security.sample.keystore;

import java.net.URL;
import java.security.KeyStore;
import java.util.Hashtable;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.auth.module.ExtensionLoginModule;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.osgi.framework.Bundle;

/**
 * Hardcoded support for Java keystore.
 */
public class KSConfigurationProvider extends Configuration {

	// Configuration name consisting of the KeyStoreLoginModule
	private static final String KEYSTORE_CONFIG_NAME = "KeyStore"; //$NON-NLS-1$

	private static final String EXT_LOGIN_MODULE_PROXY = ExtensionLoginModule.class.getName();

	private static final String KEYSTORE_FILE_EXTENSION = ".keystore";//$NON-NLS-1$

	/**
	 * Returns an AppConfigurationEntry array for the specified
	 * <code>configName</code>, or <code>null</code> if the configuration is
	 * not supported by this configuration provider.
	 * 
	 * This provider supports the default keystore configurations.
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry(String configName) {
		if (configName.equals(KEYSTORE_CONFIG_NAME))
			return new AppConfigurationEntry[] {getKeyStoreLoginEntry()};
		return null;
	}

	public void refresh() {
		// nothing to do
	}

	/**
	 * Returns a URL to the platform KeyStore.
	 */
	private URL getKeyStoreURL() {
		URL result = KeyStoreManager.getKeyStoreUrl();
		if (result == null) {
			Bundle bundle = Platform.getBundle(AuthAppPlugin.BUNDLE_ID);
			IPath path = Platform.getStateLocation(bundle).append(KEYSTORE_FILE_EXTENSION);
			try {
				result = path.toFile().toURL();
			} catch (Exception e) {
				//TODO: log it
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Returns an AppConfigurationEntry for the KeyStore LoginModule.
	 */
	private AppConfigurationEntry getKeyStoreLoginEntry() {
		Hashtable options = new Hashtable(3);
		options.put(ExtensionLoginModule.OPTION_MODULE_POINT, KeyStoreLoginModule.LOGINMODULE_POINT);
		options.put(KeyStoreLoginModule.OPTION_KEYSTORE_URL, getKeyStoreURL().toExternalForm());
		options.put(KeyStoreLoginModule.OPTION_KEYSTORE_TYPE, KeyStore.getDefaultType());

		return new AppConfigurationEntry(EXT_LOGIN_MODULE_PROXY, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
	}
}
