/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.provider.nls;

import org.eclipse.osgi.util.NLS;

public class SecProviderMessages extends NLS {

	/**
	 * The unique identifier constant of this plug-in.
	 */
	public static final String PI_AUTH = "org.eclipse.equinox.security.provider"; //$NON-NLS-1$

	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.security.provider.nls.messages"; //$NON-NLS-1$

	// general
	public static String invalidServiceListenerString;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, SecProviderMessages.class);
	}
}
