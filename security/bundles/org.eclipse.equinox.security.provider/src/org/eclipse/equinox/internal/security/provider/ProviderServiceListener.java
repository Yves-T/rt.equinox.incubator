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
package org.eclipse.equinox.internal.security.provider;

import java.security.Security;
import org.eclipse.equinox.internal.security.boot.ProviderServiceInternal;
import org.eclipse.equinox.internal.security.provider.nls.SecProviderMessages;
import org.eclipse.equinox.security.boot.ServiceProvider;
import org.eclipse.equinox.security.provider.ProviderService;
import org.osgi.framework.*;

public class ProviderServiceListener implements ServiceListener {

	private static final String FILTER_STRING = "(objectclass=" + ProviderService.class.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

	private BundleContext bundleContext;
	private ServiceProvider provider;

	private static ProviderServiceListener instance = new ProviderServiceListener();

	public static ProviderServiceListener getInstance() {
		return instance;
	}

	public static void attachServiceListener(BundleContext bundleContext) {
		getInstance().attachServiceListenerInternal(bundleContext);
	}

	private void attachServiceListenerInternal(BundleContext context) {
		try {
			provider = (ServiceProvider) Security.getProvider("EQUINOX"); //$NON-NLS-1$
			bundleContext = context;
			bundleContext.addServiceListener(this, FILTER_STRING);
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(SecProviderMessages.invalidServiceListenerString);
		}
	}

	public void serviceChanged(ServiceEvent event) {

		switch (event.getType()) {
			case ServiceEvent.REGISTERED :
				registerService(event.getServiceReference());
				break;
			case ServiceEvent.UNREGISTERING :
				unregisterService(event.getServiceReference());
				break;
			case ServiceEvent.MODIFIED :
				break;
			default :
				break;
		}
	}

	private void registerService(ServiceReference ref) {

		ProviderService service = (ProviderService) bundleContext.getService(ref);
		ProviderServiceLoader loader = new ProviderServiceLoader(ref.getBundle());

		ProviderServiceInternal internalService = service.getInternalService();
		internalService.setClassLoader(loader);
		internalService.setProvider(provider);

		provider.registerService(internalService);
	}

	private void unregisterService(ServiceReference ref) {
		//tell the provider to remove the service
	}
}
