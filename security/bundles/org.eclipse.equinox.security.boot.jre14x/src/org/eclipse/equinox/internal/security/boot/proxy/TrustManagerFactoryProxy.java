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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * OSGI service proxy implementation of <code>javax.net.ssl.TrustManagerFactorySpi</code>.
 */
public class TrustManagerFactoryProxy extends TrustManagerFactorySpi {

//	private static final Logger s_logger = Logger.getLogger( TrustManagerFactoryProxy.class.getPackage( ).toString( ));
	
	private static Logger s_logger; 
	static {
	  Class cls = TrustManagerFactoryProxy.class;
	  Package TrustManPack = cls.getPackage();
	  String packageStr = TrustManPack.toString();
	  s_logger = Logger.getLogger( packageStr);
	}
	
	private static final String ALG_NAME = "ServiceProxy";

	private static ITrustManagerFactorySpiFactory s_platformFactory;
	private TrustManagerFactorySpi targetTrustManagerFactory;
	
	/**
	 * Instantiate a new instance of TrustManagerFactoryProxy.
	 */
	public TrustManagerFactoryProxy( ) { }

	/**
	 * Return the algorithm name that is used for this service proxy.
	 * 
	 * @return	the algorithm "ServiceProxy"
	 */
	public static String getAlgorithm( ) { return ALG_NAME; }

	/**
	 * Internal interface for instantiating instances of the target TrustManagerFactorySpi
	 */
	public interface ITrustManagerFactorySpiFactory {
		TrustManagerFactorySpi newInstance( );
	}

	/**
	 * Sets the single static instance of the TrustManagerFactorySpi factory. Should only
	 * be called at plugin startup of <code>org.eclipse.equinox.security.proxy</code>.
	 * 
	 * @param platformFactory - the platform factory
	 */
	public static void setPlatformTrustManagerFactorySpiFactory( ITrustManagerFactorySpiFactory platformFactory) {
		
		if( s_logger.isLoggable( Level.FINE)) {
			s_logger.entering( TrustManagerFactoryProxy.class.toString( ), "setPlatformTrustManagerFactorySpiFactory", new Object[] {platformFactory});
		}
		
		s_platformFactory = platformFactory;
		
		if( s_logger.isLoggable( Level.FINE)) {
			s_logger.exiting(TrustManagerFactoryProxy.class.toString( ), "setPlatformTrustManagerFactorySpiFactory");
		}
	}

	private void initProxy( ) {
		if ( null == targetTrustManagerFactory) {
			targetTrustManagerFactory = s_platformFactory.newInstance( );
		}
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
	 */
	protected void engineInit( KeyStore keyStore)
		throws KeyStoreException {
		
		initProxy( );
		
		try {
			Class clazz = targetTrustManagerFactory.getClass( );
			Method method = clazz.getDeclaredMethod( "engineInit", new Class[] {KeyStore.class});
			method.setAccessible( true);
		
			method.invoke( targetTrustManagerFactory, new Object[] {keyStore});
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
			else {
				throw new RuntimeException( e);
			}
		} 
	}

	/* (non-Javadoc)
	 * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
	 */
	protected void engineInit( ManagerFactoryParameters parameters)
		throws InvalidAlgorithmParameterException {
		
		initProxy( );
		
		try {
			Class clazz = targetTrustManagerFactory.getClass( );
			Method method = clazz.getDeclaredMethod( "engineInit", new Class[] {ManagerFactoryParameters.class});
			method.setAccessible( true);
		
			method.invoke( targetTrustManagerFactory, new Object[] {parameters});
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
	 * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
	 */
	protected TrustManager[] engineGetTrustManagers( ) {
		
		initProxy( );
		
		TrustManager[] returnValue = null;
		
		try {
			Class clazz = targetTrustManagerFactory.getClass( );
			Method method = clazz.getDeclaredMethod( "engineGetTrustManagers", new Class[] {});
			method.setAccessible( true);
			
			returnValue = (TrustManager[])method.invoke( targetTrustManagerFactory, new Object[] {});
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
