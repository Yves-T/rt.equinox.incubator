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

/**
 * OSGI service based Security provider, supporting <code>java.security.KeyStoreSpi</code>,
 * <code>javax.net.ssl.TrustManagerFactorySpi</code> and <code>javax.net.ssl.KeyManagerFactorySpi</code>.
 * All can be accessed via their respective getInstance methods using the algorithm "ServiceProxy".
 */
public class ServiceProvider extends Provider {

	private static final long serialVersionUID = 6031289636819734072L;

	/**
	 * Create an instance of the ServiceProvider, which internally registers its support
	 * for the "ServiceProxy" implementations of KeyStoreSpi, TrustManagerFactorySpi and 
	 * KeyManagerFactorySpi.
	 */
	public ServiceProvider() {
		super("EQUINOX", 1.0, "Equinox OSGI Service Provider allows bundle-based implementations of JCA/JCE interfaces"); //$NON-NLS-1$//$NON-NLS-2$
	}

	private class ProviderService extends Provider.Service {

		private ProviderServiceInternal internalService;

		public ProviderService(ProviderServiceInternal internalService) {
			super(internalService.getProvider(), internalService.getType(), internalService.getAlgorithm(), internalService.getClassName(), internalService.getAliases(), internalService.getAttributes());
			this.internalService = internalService;
		}

		public Object newInstance(Object args) {
			return internalService.newInstance(args);
		}

		public boolean supportsParameter(Object arg0) {
			return internalService.supportsParameter(arg0);
		}
	}

	public void registerService(ProviderServiceInternal internalService) {
		putService(new ProviderService(internalService));
	}

	public void unregisterService(ProviderServiceInternal internalService) {
		removeService(new ProviderService(internalService));
	}

}
