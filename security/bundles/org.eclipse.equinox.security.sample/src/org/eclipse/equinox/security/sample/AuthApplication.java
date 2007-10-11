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
package org.eclipse.equinox.security.sample;

import java.security.PrivilegedAction;
import java.security.Security;
import javax.security.auth.Subject;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.security.auth.SecurePlatform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Sample application for user authorization 
 */
public class AuthApplication implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		// Set the login provider so that we can potentially use the XML provider, and 
		// set the jaas_config.xml as an available config
		Security.setProperty("login.configuration.provider", "org.eclipse.equinox.security.boot.auth.ConfigurationProvider");
		Security.setProperty("login.config.url.1", AuthAppPlugin.getBundleContext().getBundle().getEntry("data/jaas_config.xml").toExternalForm());
		//Security.setProperty( "keystore.url", AuthAppPlugin.getDefault( ).getBundle( ).getEntry( "data/test_user.jks").toExternalForm( ));

		Integer result = null;
		final Display display = PlatformUI.createDisplay();
		try {
			if (SecurePlatform.isEnabled()) {
				SecurePlatform.login();
				result = (Integer) Subject.doAs(SecurePlatform.getSubject(), getRunAction(display));
			} else
				result = (Integer) getRunAction(display).run();
		} finally {
			display.dispose();
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
