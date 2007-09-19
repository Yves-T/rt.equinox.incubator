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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.eclipse.equinox.internal.security.boot.MessageAccess;

public abstract class AbstractProxy implements InvocationHandler {

	/* Preloaded Method objects for the methods in java.lang.Object */
	private static Method hashCodeMethod;
	private static Method equalsMethod;
	private static Method toStringMethod;

	static {
		try {
			hashCodeMethod = Object.class.getMethod("hashCode", (Class[]) null); //$NON-NLS-1$
			equalsMethod = Object.class.getMethod("equals", new Class[] {Object.class}); //$NON-NLS-1$
			toStringMethod = Object.class.getMethod("toString", (Class[]) null); //$NON-NLS-1$
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(e.getMessage());
		}
	}

	public abstract Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable;

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (Object.class == method.getDeclaringClass()) {
			if (method.equals(hashCodeMethod))
				return proxyHashCode(proxy);
			if (method.equals(equalsMethod))
				return proxyEquals(proxy, args[0]);
			if (method.equals(toStringMethod))
				return proxyToString(proxy);

			throw new InternalError(MessageAccess.getString("methodNotExpected") + method.getName()); //$NON-NLS-1$
		}
		return invokeImpl(proxy, method, args);
	}

	protected Integer proxyHashCode(Object proxy) {
		return new Integer(System.identityHashCode(proxy));
	}

	protected Boolean proxyEquals(Object proxy, Object other) {
		return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
	}

	protected String proxyToString(Object proxy) {
		return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
	}

}
