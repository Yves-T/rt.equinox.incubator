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
package org.eclipse.equinox.internal.security.boot;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageAccess {
	
	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.security.messages";//$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME);

	private MessageAccess( ) { }

	public static String getString( String key) {
		
		try {
			return RESOURCE_BUNDLE.getString( key);
		}
		catch ( MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
