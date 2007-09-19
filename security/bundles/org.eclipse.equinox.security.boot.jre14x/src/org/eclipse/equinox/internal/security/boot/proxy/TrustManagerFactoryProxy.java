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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import javax.net.ssl.*;
import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;

/**
 * OSGI service proxy implementation of <code>javax.net.ssl.TrustManagerFactorySpi</code>.
 */
public class TrustManagerFactoryProxy extends TrustManagerFactorySpi {

	private static final String ALG_NAME = "PROXY"; //$NON-NLS-1$

	private static ProviderServiceInternal providerService;
	private TrustManagerFactorySpi targetTrustManagerFactory;

	/**
	 * Instantiate a new instance of TrustManagerFactoryProxy.
	 */
	public TrustManagerFactoryProxy() {
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
	 * Sets the single static instance of the TrustManagerFactorySpi factory. Should only
	 * be called at plugin startup of <code>org.eclipse.equinox.security.proxy</code>.
	 */
	public static void setProviderService(ProviderServiceInternal service) {
		providerService = service;
	}

	private void initProxy() {
		if (targetTrustManagerFactory == null)
			targetTrustManagerFactory = (TrustManagerFactorySpi) providerService.newInstance(null);
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
	 */
	protected void engineInit(KeyStore keyStore) throws KeyStoreException {
		initProxy();
		try {
			Class clazz = targetTrustManagerFactory.getClass();
			Method method = clazz.getDeclaredMethod("engineInit", new Class[] {KeyStore.class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetTrustManagerFactory, new Object[] {keyStore});
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
	 * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
	 */
	protected void engineInit(ManagerFactoryParameters parameters) throws InvalidAlgorithmParameterException {
		initProxy();
		try {
			Class clazz = targetTrustManagerFactory.getClass();
			Method method = clazz.getDeclaredMethod("engineInit", new Class[] {ManagerFactoryParameters.class}); //$NON-NLS-1$
			method.setAccessible(true);

			method.invoke(targetTrustManagerFactory, new Object[] {parameters});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof InvalidAlgorithmParameterException)
				throw (InvalidAlgorithmParameterException) target;
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
	 */
	protected TrustManager[] engineGetTrustManagers() {
		initProxy();
		TrustManager[] returnValue = null;
		try {
			Class clazz = targetTrustManagerFactory.getClass();
			Method method = clazz.getDeclaredMethod("engineGetTrustManagers", new Class[] {}); //$NON-NLS-1$
			method.setAccessible(true);

			returnValue = (TrustManager[]) method.invoke(targetTrustManagerFactory, new Object[] {});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
}
