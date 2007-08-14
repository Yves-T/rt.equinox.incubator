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
package org.eclipse.equinox.internal.security;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.equinox.internal.security.ssl.PlatformKeyManagerFactorySpiFactory;
import org.eclipse.equinox.internal.security.ssl.PlatformTrustManagerFactorySpiFactory;
import org.eclipse.equinox.security.boot.KeyStoreProxy;
import org.eclipse.equinox.security.boot.ssl.KeyManagerFactoryProxy;
import org.eclipse.equinox.security.boot.ssl.TrustManagerFactoryProxy;
import org.osgi.framework.BundleContext;


public class SecurityPlugin extends Plugin {

	private static SecurityPlugin s_instance;
	private static ResourceBundle _resourceBundle;
	  /**
	   * The constructor.
	   */
	public SecurityPlugin ( ) {
		super( );
		s_instance = this;

		try {
			_resourceBundle= ResourceBundle.getBundle("org.eclipse.equinox.internal.security.SecurityCore");
		} catch (MissingResourceException x) {
			_resourceBundle = null;
		}

	  }

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start( BundleContext context)
		throws Exception {

		super.start(context);

		/* Attach the key store proxy 
		 */
		KeyStoreProxy.IKeyStoreSpiFactory storeFactory =
			PlatformKeyStoreSpiFactory.getInstance( );
		KeyStoreProxy.setPlatformKeyStoreSpiFactory( storeFactory);
		
		/* Add the service listener for the key store Factory.
		 */
		PlatformKeyStoreSpiFactory.attachServiceListener( context);

		
		/* Attach the trust manager proxy 
		 */
		TrustManagerFactoryProxy.ITrustManagerFactorySpiFactory trustFactory =
			PlatformTrustManagerFactorySpiFactory.getInstance( );
		TrustManagerFactoryProxy.setPlatformTrustManagerFactorySpiFactory( trustFactory);
		
		/* Add the service listener for the trust manager Factory.
		 */
		PlatformTrustManagerFactorySpiFactory.attachServiceListener( context);
		
		/* Attach the key manager proxy 
		 */
		KeyManagerFactoryProxy.IKeyManagerFactorySpiFactory keyFactory =
			PlatformKeyManagerFactorySpiFactory.getInstance( );
		KeyManagerFactoryProxy.setPlatformKeyManagerFactorySpiFactory( keyFactory);
		
		/* Add the service listener for the key manager Factory.
		 */
		PlatformKeyManagerFactorySpiFactory.attachServiceListener( context);

	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop( BundleContext context)
		throws Exception {
		
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static SecurityPlugin getDefault() {
		return s_instance;
	}
	
	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= SecurityPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return _resourceBundle;
	}
}
