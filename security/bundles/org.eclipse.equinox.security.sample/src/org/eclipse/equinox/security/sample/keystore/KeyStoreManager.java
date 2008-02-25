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
import java.security.KeyStoreException;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.login.LoginException;

// this is would-be-API

public class KeyStoreManager {

	/**
	 * Return the default platform KeyStore. 
	 * @return the default platform KeyStore
	 * @throws KeyStoreException
	 * @throws LoginException
	 */
	public static KeyStore getKeyStore() throws KeyStoreException, LoginException {
		return KeyStoreManagerInternal.getInstance().getKeyStore();
	}

	/**
	 * Get the URL for the default platform KeyStore.
	 * @return the URL
	 */
	public static URL getKeyStoreUrl() {
		return KeyStoreManagerInternal.getInstance().getKeyStoreUrl();
	}

	/**
	 * Return the PBEKeySpec (a password) for the platform.
	 * @return key specification
	 * @throws LoginException
	 */
	public static PBEKeySpec getPBEKeySpec() throws LoginException {
		return KeyStoreManagerInternal.getInstance().getPBEKeySpec();
	}
}
