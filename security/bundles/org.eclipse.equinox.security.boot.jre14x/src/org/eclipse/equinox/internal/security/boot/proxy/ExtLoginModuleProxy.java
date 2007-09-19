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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.eclipse.equinox.internal.security.boot.MessageAccess;

public class ExtLoginModuleProxy extends AbstractProxy implements LoginModule {

	public interface ILoginModuleFactory {
		LoginModule getTargetLoginModule(Map options);
	}

	private static ILoginModuleFactory loginModuleFactory = null;
	private LoginModule target = null;

	public ExtLoginModuleProxy() {
		// placeholder
	}

	private ExtLoginModuleProxy(LoginModule module) {
		target = module;
	}

	private static ILoginModuleFactory getFactory() {
		if (loginModuleFactory == null)
			throw new RuntimeException(MessageAccess.getString("loginmoduleFactoryNotSet")); //$NON-NLS-1$
		return loginModuleFactory;
	}

	public static void setFactory(ILoginModuleFactory factory) {
		loginModuleFactory = factory;
	}

	private static Object newProxyInstance(Map options) {
		LoginModule target = getFactory().getTargetLoginModule(options);

		Object returnValue = Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), new ExtLoginModuleProxy(target));
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.security.jaas.BaseProxy#invokeImpl(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable {
		if (target == null)
			throw new LoginException(MessageAccess.getString("loginmoduleNotSet")); //$NON-NLS-1$
		return method.invoke(target, args);
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
		target = (LoginModule) ExtLoginModuleProxy.newProxyInstance(options);
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
}
