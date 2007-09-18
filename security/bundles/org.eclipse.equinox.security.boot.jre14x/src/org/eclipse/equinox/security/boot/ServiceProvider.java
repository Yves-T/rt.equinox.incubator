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
package org.eclipse.equinox.security.boot;

import java.security.Provider;

import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;

public class ServiceProvider extends Provider {

	private static final long serialVersionUID = -696520082946329858L;

	public ServiceProvider() {
		super("EQUINOX", 0.1, "");
	}

	public void registerService(ProviderServiceInternal providerService) {
		String key = providerService.getAlgorithm() + "." + providerService.getType();
		this.put(key, providerService.getClassName());
	}
	
	public void unregisterService(ProviderServiceInternal providerService) {
		//TODO:
	}
}
