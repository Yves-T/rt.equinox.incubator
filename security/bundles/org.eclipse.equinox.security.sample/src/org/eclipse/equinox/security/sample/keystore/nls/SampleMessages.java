/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample.keystore.nls;

import org.eclipse.osgi.util.NLS;

public class SampleMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.equinox.security.sample.keystore.nls.messages"; //$NON-NLS-1$

	// General use
	public static String enterPasswordLabel;
	public static String confirmPasswordLabel;
	public static String enterKeystorePassword;
	public static String passwordLabel;
	public static String callbackhandlerUnavailable;
	public static String passwordRequired;
	public static String passwordNomatch;
	public static String enterPassword;

	// Configuration loaders
	public static String invalidKeystore;
	public static String invalidURL;

	// Keystore dialog
	public static String loginButton;
	public static String exitButton;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, SampleMessages.class);
	}
}