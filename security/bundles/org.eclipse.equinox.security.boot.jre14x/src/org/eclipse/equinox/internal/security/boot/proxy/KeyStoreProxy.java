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
package org.eclipse.equinox.internal.security.boot.proxy;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;

/**
 * OSGI service proxy implementation of <code>java.security.KeyStoreSpi</code>.
 */
public class KeyStoreProxy extends KeyStoreSpi {

	private static final String ALG_NAME = "PROXY"; //$NON-NLS-1$

	private static ProviderServiceInternal internalService;
	private KeyStoreSpi targetKeyStoreSpi;

	/**
	 * Instantiate a new instance of KeyStoreProxy.
	 */
	public KeyStoreProxy() {
		// placeholder
	}

	/**
	 * Return the algorithm name that is used for this service proxy.
	 * 
	 * @return	the algorithm "ServiceProxy"
	 */
	public static String getAlgorithm() {
		return ALG_NAME;
	}

	/**
	 * Sets the single static instance of the ProviderServiceInternal.
	 * 
	 * @param platformFactory - the platform factory
	 */
	public static void setProviderService(ProviderServiceInternal service) {
		internalService = service;
	}

	private void initProxy() {
		if (targetKeyStoreSpi == null)
			targetKeyStoreSpi = (KeyStoreSpi) internalService.newInstance(null);
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineLoad(java.io.InputStream, char[])
	 */
	public void engineLoad(InputStream inputStream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
		initProxy();
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineLoad", new Class[] {InputStream.class, char[].class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetKeyStoreSpi, new Object[] {inputStream, password});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof IOException) {
				throw (IOException) target;
			} else if (target instanceof NoSuchAlgorithmException) {
				throw (NoSuchAlgorithmException) target;
			} else if (target instanceof CertificateException) {
				throw (CertificateException) target;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineStore(java.io.OutputStream, char[])
	 */
	public void engineStore(OutputStream outputStream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
		initProxy();
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineStore", new Class[] {OutputStream.class, char[].class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetKeyStoreSpi, new Object[] {outputStream, password});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof IOException) {
				throw (IOException) target;
			} else if (target instanceof NoSuchAlgorithmException) {
				throw (NoSuchAlgorithmException) target;
			} else if (target instanceof CertificateException) {
				throw (CertificateException) target;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSize()
	 */
	public int engineSize() {
		initProxy();
		int returnValue = 0;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineSize", new Class[] {}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = ((Integer) method.invoke(targetKeyStoreSpi, new Object[] {})).intValue();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineAliases()
	 */
	public Enumeration engineAliases() {
		initProxy();
		Enumeration returnValue = null;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineAliases", new Class[] {}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (Enumeration) method.invoke(targetKeyStoreSpi, new Object[] {});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineContainsAlias(java.lang.String)
	 */
	public boolean engineContainsAlias(String alias) {
		initProxy();
		boolean returnValue = false;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineContainsAlias", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = ((Boolean) method.invoke(targetKeyStoreSpi, new Object[] {alias})).booleanValue();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineIsKeyEntry(java.lang.String)
	 */
	public boolean engineIsKeyEntry(String alias) {
		initProxy();
		boolean returnValue = false;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineIsKeyEntry", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = ((Boolean) method.invoke(targetKeyStoreSpi, new Object[] {alias})).booleanValue();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineIsCertificateEntry(java.lang.String)
	 */
	public boolean engineIsCertificateEntry(String alias) {
		initProxy();
		boolean returnValue = false;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineIsCertificateEntry", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = ((Boolean) method.invoke(targetKeyStoreSpi, new Object[] {alias})).booleanValue();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetKey(java.lang.String, char[])
	 */
	public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
		initProxy();
		Key returnValue = null;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineGetKey", new Class[] {String.class, char[].class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (Key) method.invoke(targetKeyStoreSpi, new Object[] {alias, password});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof NoSuchAlgorithmException) {
				throw (NoSuchAlgorithmException) target;
			} else if (target instanceof UnrecoverableKeyException) {
				throw (UnrecoverableKeyException) target;
			} else {
				throw new RuntimeException(e);
			}
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSetKeyEntry(java.lang.String, java.security.Key, char[], java.security.cert.Certificate[])
	 */
	public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
		initProxy();
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineSetKeyEntry", new Class[] {String.class, Key.class, char[].class, Certificate[].class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetKeyStoreSpi, new Object[] {alias, key, password, chain});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof KeyStoreException)
				throw (KeyStoreException) target;
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSetKeyEntry(java.lang.String, byte[], java.security.cert.Certificate[])
	 */
	public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
		initProxy();
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineSetKeyEntry", new Class[] {String.class, byte[].class, Certificate[].class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetKeyStoreSpi, new Object[] {alias, key, chain});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof KeyStoreException)
				throw (KeyStoreException) target;
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCertificateAlias(java.security.cert.Certificate)
	 */
	public String engineGetCertificateAlias(Certificate cert) {
		initProxy();
		String returnValue = null;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineGetCertificateAlias", new Class[] {Certificate.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (String) method.invoke(targetKeyStoreSpi, new Object[] {cert});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;

	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCertificate(java.lang.String)
	 */
	public Certificate engineGetCertificate(String alias) {
		initProxy();
		Certificate returnValue = null;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineGetCertificate", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (Certificate) method.invoke(targetKeyStoreSpi, new Object[] {alias});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCertificateChain(java.lang.String)
	 */
	public Certificate[] engineGetCertificateChain(String alias) {
		initProxy();
		Certificate[] returnValue = null;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineGetCertificateChain", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (Certificate[]) method.invoke(targetKeyStoreSpi, new Object[] {alias});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCreationDate(java.lang.String)
	 */
	public Date engineGetCreationDate(String alias) {
		initProxy();
		Date returnValue = null;
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineGetCreationDate", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (Date) method.invoke(targetKeyStoreSpi, new Object[] {alias});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSetCertificateEntry(java.lang.String, java.security.cert.Certificate)
	 */
	public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
		initProxy();
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineSetCertificateEntry", new Class[] {String.class, Certificate.class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetKeyStoreSpi, new Object[] {alias, cert});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof KeyStoreException)
				throw (KeyStoreException) target;
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineDeleteEntry(java.lang.String)
	 */
	public void engineDeleteEntry(String alias) throws KeyStoreException {
		initProxy();
		try {
			Class clazz = targetKeyStoreSpi.getClass();
			Method method = clazz.getDeclaredMethod("engineDeleteEntry", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetKeyStoreSpi, new Object[] {alias});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof KeyStoreException)
				throw (KeyStoreException) target;
			throw new RuntimeException(e);
		}
	}
}
