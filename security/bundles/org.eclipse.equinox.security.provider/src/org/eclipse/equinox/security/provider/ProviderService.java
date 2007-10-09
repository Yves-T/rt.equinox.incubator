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
package org.eclipse.equinox.security.provider;

import java.lang.reflect.Constructor;
import java.security.*;
import java.util.List;
import java.util.Map;
import org.eclipse.equinox.security.boot.IProviderService;
import org.osgi.framework.Bundle;

/**
 * The class is used to register a security service with the system.
 */
public class ProviderService implements IProviderService {
	public final String EQUINOX_PROVIDER = "EQUINOX"; //$NON-NLS-1$
	private final String type;
	private final String algorithm;
	private final String className;
	private final List aliases;
	private final Map attributes;
	private final Bundle providingBundle;

	public ProviderService(String type, String algorithm, String className, List aliases, Map attributes, Bundle providingBundle) {
		this.type = type;
		this.algorithm = algorithm;
		this.className = className;
		this.aliases = aliases;
		this.attributes = attributes;
		this.providingBundle = providingBundle;
	}

	public String getType() {
		return type;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getClassName() {
		return className;
	}

	public List getAliases() {
		return aliases;
	}

	public Map getAttributes() {
		return attributes;
	}

	public Provider getProvider() {
		return Security.getProvider(EQUINOX_PROVIDER);
	}

	public Object newInstance(Object parameter) throws NoSuchAlgorithmException {
		Object obj = null;
		try {
			Class clazz = providingBundle.loadClass(getClassName());
			if (parameter != null) {
				try {
					Constructor cons = clazz.getDeclaredConstructor(new Class[] {parameter.getClass()});
					obj = cons.newInstance(new Object[] {parameter});
				} catch (NoSuchMethodException e) {
					throw new InvalidParameterException();
				}
			} else {
				obj = clazz.newInstance();
			}
		} catch (Throwable t) {
			throw new NoSuchAlgorithmException();
		}

		return obj;
	}

	public boolean supportsParameter(Object parameter) {
		try {
			Class clazz = providingBundle.loadClass(getClassName());
			clazz.getDeclaredConstructor(new Class[] {parameter.getClass()});
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

}
