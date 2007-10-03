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

import java.security.Provider;
import java.util.List;
import java.util.Map;
import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;

/**
 * The class is used to register a security service with the system.
 */
public class ProviderService {

	private ProviderServiceInternal internalService;

	public ProviderService(String type, String algorithm, String className, List aliases, Map attributes) {
		internalService = new ProviderServiceInternal(type, algorithm, className, aliases, attributes);
	}

	public String getAlgorithm() {
		return internalService.getAlgorithm();
	}

	public String getAttribute(String name) {
		return (String) internalService.getAttributes().get(name);
	}

	public Provider getProvider() {
		return internalService.getProvider();
	}

	public String getType() {
		return internalService.getType();
	}

	public Object newInstance(Object parameter) {
		return internalService.newInstance(parameter);
	}

	public boolean supportsParameter(Object parameter) {
		return internalService.supportsParameter(parameter);
	}

	public String toString() {
		return internalService.toString();
	}

	//TODO: this can't be exposed
	public ProviderServiceInternal getInternalService() {
		return internalService;
	}
}
