/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.auth.ISecureContext;
import org.eclipse.equinox.security.auth.SecurePlatform;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.eclipse.equinox.security.sample.keystore.nls.SampleMessages;
import org.osgi.framework.Bundle;

public class KeyStoreManagerInternal {
	/**
	 * The name of the KeyStore configuration
	 */
	private static final String CONFIG_NAME_KEYSTORE = "KeyStore"; //$NON-NLS-1$

	private static final KeyStoreManagerInternal s_instance = new KeyStoreManagerInternal();

	private URL keyStoreUrl;
	private KeyStore keyStore;

	private ISecureContext secureContext = null;

	private KeyStoreManagerInternal() {
		// hides default constructor
	}

	public static final KeyStoreManagerInternal getInstance() {
		return s_instance;
	}

	public URL getKeyStoreUrl() {
		if (keyStoreUrl == null) {
			String url = Security.getProperty("keystore.url"); //$NON-NLS-1$
			if (url != null) {
				try {
					keyStoreUrl = new URL(url);
				} catch (Exception e) {
					throw new IllegalArgumentException(SampleMessages.invalidKeystore);
				}
			} else {
				Bundle bundle = Platform.getBundle(AuthAppPlugin.PI_AUTH);
				IPath path = Platform.getStateLocation(bundle).append(".keystore"); //$NON-NLS-1$
				try {
					keyStoreUrl = path.toFile().toURL();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return keyStoreUrl;
	}

	public KeyStore getKeyStore() throws KeyStoreException {
		if (keyStore == null) {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream is = null;
			try {
				URL url = getKeyStoreUrl();
				if (url != null) {
					try {
						is = url.openStream();
					} catch (IOException ioe) {
						// pass a null input stream to the keystore and let it handle the null 
						// value either by throwing an exception, or ignoring the value
					}
				}
				keyStore.load(is, getPBEKeySpec().getPassword());
			} catch (Exception e) {
				KeyStoreException e1 = new KeyStoreException();
				e1.initCause(e);
				throw e1;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e) {
						// ignore - nothing can be done
					}
				}
			}
		}
		return keyStore;
	}

	public ISecureContext getSecureContext() {
		if (secureContext == null)
			secureContext = SecurePlatform.createContext(CONFIG_NAME_KEYSTORE);
		return secureContext;
	}

	public PBEKeySpec getPBEKeySpec() throws LoginException {
		// not necessary
		// getSecureContext().login();

		// This implies that the contract of the platform logincontext
		// is to provide a PBEKeySpecCredential for unlocking the default keystore!
		Subject subject = getSecureContext().getSubject();
		Set passwords = subject.getPrivateCredentials(KeyStoreCredential.class);

		PBEKeySpec returnValue = null;
		if ((passwords != null) && (passwords.size() > 0)) {
			for (Iterator it = passwords.iterator(); it.hasNext();) {
				Object obj = it.next();
				if (!(obj instanceof KeyStoreCredential))
					continue;
				KeyStoreCredential cred = (KeyStoreCredential) obj;
				if (KeyStore.getDefaultType().equals(cred.getType()) && getKeyStoreUrl().equals(cred.getUrl())) {
					returnValue = cred.getKeySpec();
					break;
				}
			}
		}
		if (returnValue == null)
			returnValue = new PBEKeySpec(new char[0]);

		return returnValue;
	}

}
