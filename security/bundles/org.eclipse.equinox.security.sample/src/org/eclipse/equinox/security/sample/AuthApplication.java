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
import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.equinox.security.auth.SecurePlatform;
import org.eclipse.equinox.security.auth.service.ILoginContextService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;


/**
 * This class controls all aspects of the application's execution
 */
public class AuthApplication implements IPlatformRunnable {

	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run( Object args) throws Exception {

		Object returnValue = null;
		
		try {
			/* Set the login provider so that we can potentially use the XML provider, and set the jaas_config.xml as an available config */ 
			Security.setProperty( "login.configuration.provider", "org.eclipse.equinox.security.auth.ConfigurationProvider");
			Security.setProperty( "login.config.url.1", AuthAppPlugin.getDefault( ).getBundle( ).getEntry( "data/jaas_config.xml").toExternalForm( ));
			//Security.setProperty( "keystore.url", AuthAppPlugin.getDefault( ).getBundle( ).getEntry( "data/test_user.jks").toExternalForm( ));
			
			final Display display = PlatformUI.createDisplay( );
			
			if ( SecurePlatform.isEnabled( )) {
				ILoginContextService loginContext = SecurePlatform.getLoginContext( );
				loginContext.login( );
				returnValue = (Integer)Subject.doAs( loginContext.getSubject( ), getRunAction( display));
			}
			else {
				returnValue = (Integer)getRunAction( display).run( );
			}
		}
		catch ( LoginException le) { }
		
		return returnValue;
	}
		
	private PrivilegedAction getRunAction( final Display display) {
		
		PrivilegedAction returnValue = new PrivilegedAction( ) {
			
			/* (non-Javadoc)
			 * @see java.security.PrivilegedAction#run()
			 */
			public Object run( ) {
				try {
					int returnCode = PlatformUI.createAndRunWorkbench( display, new AuthWorkbenchAdvisor( ));
					if ( returnCode == PlatformUI.RETURN_RESTART) {
						return IPlatformRunnable.EXIT_RESTART;
					}
				}
				finally {
					display.dispose( );
				}
				return IPlatformRunnable.EXIT_OK;
			}
		};
		
		return returnValue;
	}
}
