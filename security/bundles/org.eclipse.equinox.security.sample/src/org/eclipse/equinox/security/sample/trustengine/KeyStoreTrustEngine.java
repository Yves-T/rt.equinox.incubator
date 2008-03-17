/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample.trustengine;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import org.eclipse.osgi.service.security.TrustEngine;

//*potential enhancements*
// 1. reloading from the backing file when it changes
// 3. methods to support lock/unlock
//  3a. Using a callback handler to collect the password
//  3b. managing lock/unlock between multiple threads. dealing with SWT UI thread
// 4. methods to support changing password, etc
// 5. methods to support export, etc
// 6. 'friendly-name' generator
// 7. Listeners for change events
public class KeyStoreTrustEngine extends TrustEngine {

	private KeyStore keyStore;
	private char[] password;
	private final URL url;
	private final String name;

	public KeyStoreTrustEngine(KeyStore keyStore, URL url, char[] password, String name) {
		this.keyStore = keyStore;
		this.url = url;
		this.password = password;
		this.name = name;
	}

	/**
	 * Return the type
	 * @return type - the type for the KeyStore being managed
	 */
	private String getType() {
		return keyStore.getType();
	}

	/**
	 * Return the path
	 * @return - the path for the KeyStore being managed
	 */
	private URL getUrl() {
		return url;
	}

	/**
	 * Return the password
	 * @return password - the password as a char[] 
	 */
	private char[] getPassword() {
		return password;
	}

	/**
	 * Return the KeyStore managed
	 * @return keystore - the KeyStore instance, initialized and loaded
	 * @throws KeyStoreException
	 */
	private synchronized KeyStore getKeyStore() throws IOException, GeneralSecurityException {
		if (null == keyStore) {
			keyStore = KeyStore.getInstance(getType());
			loadStore(keyStore, getInputStream());
		}

		if (keyStore == null)
			throw new KeyStoreException("");

		return keyStore;
	}

	public Certificate findTrustAnchor(Certificate[] certChain) throws IOException {

		if (certChain == null || certChain.length == 0)
			throw new IllegalArgumentException("Certificate chain is required"); //$NON-NLS-1$

		try {
			KeyStore store = getKeyStore();
			for (int i = 0; i < certChain.length; i++) {
				if (i == certChain.length - 1) {
					certChain[i].verify(certChain[i].getPublicKey());
				} else {
					X509Certificate nextX509Cert = (X509Certificate) certChain[i + 1];
					certChain[i].verify(nextX509Cert.getPublicKey());
				}

				synchronized (store) {
					String alias = store.getCertificateAlias(certChain[i]);
					if (alias != null) {
						return store.getCertificate(alias);
					}
				}
			}
		} catch (KeyStoreException e) {
			throw new IOException(e.getMessage());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	protected String doAddTrustAnchor(Certificate cert, String alias) throws IOException, GeneralSecurityException {
		if (isReadOnly())
			throw new IOException("");
		if (cert == null) {
			throw new IllegalArgumentException("Certificate must be specified"); //$NON-NLS-1$
		}
		try {
			KeyStore store = getKeyStore();
			synchronized (store) {
				String oldAlias = store.getCertificateAlias(cert);
				if (null != oldAlias)
					throw new CertificateException("");
				Certificate oldCert = store.getCertificate(alias);
				if (null != oldCert)
					throw new CertificateException("");
				store.setCertificateEntry(alias, cert);
				saveStore(store, getOutputStream());
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
		return alias;
	}

	protected void doRemoveTrustAnchor(Certificate cert) throws IOException, GeneralSecurityException {
		if (isReadOnly())
			throw new IOException("");
		if (cert == null) {
			throw new IllegalArgumentException("Certificate must be specified"); //$NON-NLS-1$
		}
		try {
			KeyStore store = getKeyStore();
			synchronized (store) {
				String alias = store.getCertificateAlias(cert);
				if (alias == null) {
					throw new CertificateException("");
				}
				removeTrustAnchor(alias);
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
	}

	protected void doRemoveTrustAnchor(String alias) throws IOException, GeneralSecurityException {

		if (alias == null) {
			throw new IllegalArgumentException("Alias must be specified"); //$NON-NLS-1$
		}
		try {
			KeyStore store = getKeyStore();
			synchronized (store) {
				Certificate oldCert = store.getCertificate(alias);
				if (oldCert == null)
					throw new CertificateException("");
				store.deleteEntry(alias);
				saveStore(store, getOutputStream());
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
	}

	public Certificate getTrustAnchor(String alias) throws IOException, GeneralSecurityException {

		if (alias == null) {
			throw new IllegalArgumentException("Alias must be specified"); //$NON-NLS-1$
		}

		try {
			KeyStore store = getKeyStore();
			synchronized (store) {
				return store.getCertificate(alias);
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
	}

	public String[] getAliases() throws IOException, GeneralSecurityException {

		ArrayList returnList = new ArrayList();
		try {
			KeyStore store = getKeyStore();
			synchronized (store) {
				for (Enumeration aliases = store.aliases(); aliases.hasMoreElements();) {
					String currentAlias = (String) aliases.nextElement();
					if (store.isCertificateEntry(currentAlias)) {
						returnList.add(currentAlias);
					}
				}
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
		return (String[]) returnList.toArray(new String[] {});
	}

	/**
	 * Load using the current password
	 */
	private void loadStore(KeyStore store, InputStream is) throws IOException, GeneralSecurityException {
		store.load(is, getPassword());
	}

	/**
	 * Save using the current password
	 */
	private void saveStore(KeyStore store, OutputStream os) throws IOException, GeneralSecurityException {
		store.store(os, getPassword());
	}

	/**
	 * Get an input stream for the KeyStore managed
	 * @return inputstream - the stream
	 * @throws KeyStoreException
	 */
	private InputStream getInputStream() throws IOException {
		if ("file".equals(getUrl().getProtocol())) {
			return new FileInputStream(new File(getUrl().toExternalForm()));
		} else {
			throw new IOException();
		}
	}

	/**
	 * Get an output stream for the KeyStore managed
	 * @return outputstream - the stream
	 * @throws KeyStoreException
	 */
	private OutputStream getOutputStream() throws IOException {

		if ("file".equals(getUrl().getProtocol())) {
			File file = new File(getUrl().getPath());
			if (!file.exists())
				file.createNewFile();

			return new FileOutputStream(file);
		} else {
			throw new IOException("");
		}
	}

	public boolean isReadOnly() {
		if ("file".equals(getUrl().getProtocol())) {
			return getPassword() == null || !(new File(getUrl().getPath()).canWrite());
		} else {
			return true;
		}
	}

	public String getName() {
		return name;
	}
}
