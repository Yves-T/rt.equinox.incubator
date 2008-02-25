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
import javax.crypto.spec.PBEKeySpec;

/**
 * A class to represent the password (in PBEKey format) for a particular KeyStore.
 */
public class KeyStoreCredential {

	private String type;
	private URL url;
	private PBEKeySpec spec;

	/**
	 * Create a new KeyStoreCredential.
	 * @param type the KeyStore type
	 * @param url the URL to the KeyStore (or null for 'NONE')
	 */
	public KeyStoreCredential(String type, URL url) {
		this.type = type;
		this.url = url;
	}

	/**
	 * Get the type for the KeyStore.
	 * @return	the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Return the URL for the KeyStore, or null if not appropriate.
	 * @return the URL
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Return the PBEKeySpec (a password) for the KeyStore.
	 * @return the key spec
	 */
	public PBEKeySpec getKeySpec() {
		return spec;
	}

	/**
	 * Set the PBEKeySpec (a password) for the KeyStore.
	 * @param spec
	 */
	public void setKeySpec(PBEKeySpec spec) {
		this.spec = spec;
	}

	/* (non-JavaDoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = type.toUpperCase().hashCode();
		if (url != null)
			result += url.toString().hashCode();
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object another) {
		if (!(another instanceof KeyStoreCredential))
			return false;

		KeyStoreCredential anotherCredential = (KeyStoreCredential) another;
		String anotherType = anotherCredential.getType();
		if (anotherType == null || !anotherType.equalsIgnoreCase(type))
			return false;

		URL anotherURL = anotherCredential.getUrl();
		if (anotherURL == null)
			return (url == null);
		return anotherURL.equals(this.url);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = type.toUpperCase();
		if (url != null)
			result += ":" + url.toString(); //$NON-NLS-1$
		return result;
	}
}
