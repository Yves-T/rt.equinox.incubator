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
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;

import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;

/**
 * OSGI service proxy implementation of <code>javax.net.ssl.KeyManagerFactorySpi</code>.
 */
public class KeyManagerFactoryProxy extends KeyManagerFactorySpi {

	// private static Logger s_logger = Logger.getLogger( KeyManagerFactoryProxy.class.getPackage( ).toString( ));
	
	private static Logger s_logger; 
	static {
	  Class cls = KeyManagerFactoryProxy.class;
	  Package keyManPack = cls.getPackage();
	  String packageStr = keyManPack.toString();
	  s_logger = Logger.getLogger( packageStr);
	}
	
	private static final String ALG_NAME = "PROXY";

	/**
	 * Instantiate a new instance of KeyManagerFactoryProxy.
	 */
	public KeyManagerFactoryProxy( ) { }
	
	/**
	 * Return the algorithm name that is used for this service proxy.
	 * 
	 * @return	the algorithm "ServiceProxy"
	 */
	public static String getAlgorithm( ) { return ALG_NAME; }

	private static ProviderServiceInternal s_providerService;
	private static KeyManagerFactorySpi targetKeyManagerFactory;

	private void initProxy( ) {
		if ( null == targetKeyManagerFactory) {
			targetKeyManagerFactory = (KeyManagerFactorySpi)s_providerService.newInstance( null);
		}
	}
	
	/**
	 * Sets the single static instance of the ProviderServiceInternal
	 * 
	 * @param providerService - the ProviderServiceInternal
	 */
	public static void setProviderService( ProviderServiceInternal providerService) {
		
		if (s_logger.isLoggable( Level.FINE)) {
			s_logger.entering( KeyManagerFactoryProxy.class.toString( ), "setProviderService", new Object[] {providerService});
		}

		s_providerService = providerService;

		if (s_logger.isLoggable(Level.FINE)) {
			s_logger.exiting( KeyManagerFactoryProxy.class.toString( ), "setProviderService");
		}
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.KeyManagerFactorySpi#engineInit(java.security.KeyStore, char[])
	 */
	protected void engineInit( KeyStore keyStore, char[] password)
		throws KeyStoreException, NoSuchAlgorithmException,	UnrecoverableKeyException {
		
		initProxy( );
		
		try {
			Class clazz = targetKeyManagerFactory.getClass( );
			Method method = clazz.getDeclaredMethod( "engineInit", new Class[] {KeyStore.class,char[].class});
			method.setAccessible( true);
			
			method.invoke( targetKeyManagerFactory, new Object[] {keyStore,password});
		}
		catch ( NoSuchMethodException e) {
			throw new RuntimeException( e);
		}
		catch ( IllegalAccessException e) {
			throw new RuntimeException( e);
		}
		catch ( InvocationTargetException e) {
			Throwable target = e.getTargetException( );
			if ( target instanceof KeyStoreException) {
				throw (KeyStoreException)target;
			}
			else if ( target instanceof NoSuchAlgorithmException) {
				throw (NoSuchAlgorithmException)target;
			}
			else if ( target instanceof UnrecoverableKeyException) {
				throw (UnrecoverableKeyException)target;
			}
			else {
				throw new RuntimeException( e);
			}
		} 
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.KeyManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
	 */
	protected void engineInit( ManagerFactoryParameters parameters)
			throws InvalidAlgorithmParameterException {
		
		initProxy( );
		
		try {
			Class clazz = targetKeyManagerFactory.getClass( );
			Method method = clazz.getDeclaredMethod( "engineInit", new Class[] {ManagerFactoryParameters.class});
			method.setAccessible( true);
			
			method.invoke( targetKeyManagerFactory, new Object[] {parameters});
		}
		catch ( NoSuchMethodException e) {
			throw new RuntimeException( e);
		}
		catch ( IllegalAccessException e) {
			throw new RuntimeException( e);
		}
		catch ( InvocationTargetException e) {
			Throwable target = e.getTargetException( );
			if ( target instanceof InvalidAlgorithmParameterException) {
				throw (InvalidAlgorithmParameterException)target;
			}
			else {
				throw new RuntimeException( e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.KeyManagerFactorySpi#engineGetKeyManagers()
	 */
	protected KeyManager[] engineGetKeyManagers( ) {
		
		initProxy( );
		
		KeyManager[] returnValue = new KeyManager[] {};
		
		try {
			Class clazz = targetKeyManagerFactory.getClass( );
			Method method = clazz.getDeclaredMethod( "engineGetKeyManagers", new Class[] {});
			method.setAccessible( true);
			
			returnValue = (KeyManager[])method.invoke( targetKeyManagerFactory, new Object[] {});
		}
		catch ( NoSuchMethodException e) {
			throw new RuntimeException( e);
		}
		catch ( IllegalAccessException e) {
			throw new RuntimeException( e);
		}
		catch ( InvocationTargetException e) {
			throw new RuntimeException( e);
		}
		
		return returnValue;
	}
}
