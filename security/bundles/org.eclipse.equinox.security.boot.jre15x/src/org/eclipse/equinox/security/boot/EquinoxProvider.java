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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * The Equinox security provider.  This provider implementation sits a layer below the Framework
 * and allows provider services to be registered by the Framework and other bundles installed
 * in the Framework with {@link IProviderService} objects.
 * 
 * <p>
 * Clients are not intended to create or use this class.
 * </p>
 */
// TODO need to clean this javadoc up.
// TODO Perhaps this class and IProviderService should be in separate packages to give a clear indication of API for clients.
public class EquinoxProvider extends Provider {

	private static final long serialVersionUID = 6031289636819734072L;

	/**
	 * Default constructor for the Equinox provider registry
	 */
	public EquinoxProvider() {
		super("EQUINOX", 1.0, "Equinox OSGI Service Provider allows bundle-based implementations of JCA/JCE interfaces"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Registers a new provider service with the Equinox provider registry
	 * @param service the provider service to register
	 */
	public void registerService(IProviderService service) {
		putService(new ProviderService(service));

	}

	public void unregisterService(IProviderService service) {
		removeService(new ProviderService(service));
	}

	private static class ProviderService extends Provider.Service {

		private final IProviderService service;

		public ProviderService(IProviderService service) {
			super(service.getProvider(), service.getType(), service.getAlgorithm(), service.getClassName(), service.getAliases(), service.getAttributes());
			this.service = service;
		}

		public Object newInstance(Object parameter) throws NoSuchAlgorithmException {
			return service.newInstance(parameter);
		}

		public boolean supportsParameter(Object parameter) {
			return service.supportsParameter(parameter);
		}
	}
}
