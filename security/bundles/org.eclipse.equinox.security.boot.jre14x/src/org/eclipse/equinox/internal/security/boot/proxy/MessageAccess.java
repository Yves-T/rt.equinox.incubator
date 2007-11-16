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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * In the class loading hierarchy this fragment is placed below framework. As such,
 * the normal logging (supplied by Runtime), OSGi logging (supplied by the framework),
 * and NLS mechanism (supplied by the framework) are not available.
 * 
 * Hence, this fragment does not log errors but throws exceptions allowing logging
 * to be done by callers.
 * 
 * As NLS is not available, this bundle uses Java resource bundles for internationalization.
 */
public class MessageAccess {

	// XXX this class is not used at present; consider removing
	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.security.boot.proxy.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
