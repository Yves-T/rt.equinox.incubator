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
package org.eclipse.equinox.internal.security.provider;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class SecurityPlugin implements BundleActivator {

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

		ProviderServiceListener.attachServiceListener(context);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop( BundleContext context)
		throws Exception {
		
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
