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

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class ExtLoginModuleProxy implements LoginModule {

	public interface ILoginModuleFactory {
		LoginModule getTargetLoginModule(Map<String, ?> options);
	}

	private static ILoginModuleFactory loginModuleFactory = null;
	private static Object lock = new Object();

	private LoginModule target = null;

	public ExtLoginModuleProxy() {
		// placeholder
	}

	private static ILoginModuleFactory getFactory() {
		if (loginModuleFactory == null)
			throw new RuntimeException(MessageAccess.getString("loginmoduleFactoryNotSet")); //$NON-NLS-1$
		return loginModuleFactory;
	}

	public static void setFactory(ILoginModuleFactory factory) {
		synchronized (lock) {
			if (loginModuleFactory != null)
				throw new RuntimeException(MessageAccess.getString("loginmoduleFactoryAlreadySet")); //$NON-NLS-1$
			loginModuleFactory = factory;
		}
	}

	public static void unsetFactory(ILoginModuleFactory factory) {
		synchronized (lock) {
			if (loginModuleFactory != factory)
				throw new SecurityException(MessageAccess.getString("unsetLoginmoduleFactoryError")); //$NON-NLS-1$
			loginModuleFactory = null;
		}
	}

	private static Object getTargetLoginModule(Map<String, ?> options) {
		synchronized (lock) {
			return getFactory().getTargetLoginModule(options);
		}
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
		target = (LoginModule) ExtLoginModuleProxy.getTargetLoginModule(options);
		target.initialize(subject, callbackHandler, sharedState, options);
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#login()
	 */
	public boolean login() throws LoginException {
		return target.login();
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#commit()
	 */
	public boolean commit() throws LoginException {
		return target.commit();
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#abort()
	 */
	public boolean abort() throws LoginException {
		return target.abort();
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#logout()
	 */
	public boolean logout() throws LoginException {
		return target.logout();
	}

	public String toString() {
		return target.toString();
	}
}
