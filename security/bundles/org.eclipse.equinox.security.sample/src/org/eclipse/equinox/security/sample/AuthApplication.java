/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample;

import java.net.URL;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.security.auth.ISecureContext;
import org.eclipse.equinox.security.auth.SecurePlatform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Sample application for user authorization 
 */
public class AuthApplication implements IApplication {

	// TBD this string should be an API on the provider - auth bundle
	/**
	 * The name of the KeyStore configuration
	 */
	private static final String CONFIG_NAME_KEYSTORE = "KeyStore"; //$NON-NLS-1$

	/**
	 * The name of the "native" configuration described in this application's config file
	 */
	private static final String CONFIG_NAME_WIN32 = "Win32"; //$NON-NLS-1$

	/**
	 * Specifies location of the login configuration file for this application
	 */
	private static final String JAAS_CONFIG_FILE = "data/jaas_config.txt"; //$NON-NLS-1$

	public Object start(IApplicationContext context) throws Exception {
		ISecureContext secureContext;

		if (false) { // two test cases: {KeyStore & hardcoded config} or {native & config from a file}
			secureContext = SecurePlatform.createContext(CONFIG_NAME_KEYSTORE);
		} else {
			URL configFile = AuthAppPlugin.getBundleContext().getBundle().getEntry(JAAS_CONFIG_FILE);
			secureContext = SecurePlatform.createContext(CONFIG_NAME_WIN32, configFile);
		}

		secureContext.registerListener(new ProgressMonitorListener());

		//Security.setProperty( "keystore.url", AuthAppPlugin.getDefault( ).getBundle( ).getEntry( "data/test_user.jks").toExternalForm( ));

		Integer result = null;
		final Display display = PlatformUI.createDisplay();
		try {
			result = (Integer) Subject.doAs(secureContext.getSubject(), getRunAction(display));
		} finally {
			display.dispose();
			secureContext.logout();
		}
		// TBD handle javax.security.auth.login.LoginException

		if (result != null && PlatformUI.RETURN_RESTART == result.intValue())
			return EXIT_RESTART;
		return EXIT_OK;
	}

	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	private PrivilegedAction getRunAction(final Display display) {
		return new PrivilegedAction() {
			public Object run() {
				int result = PlatformUI.createAndRunWorkbench(display, new AuthWorkbenchAdvisor());
				return new Integer(result);
			}
		};
	}

}
