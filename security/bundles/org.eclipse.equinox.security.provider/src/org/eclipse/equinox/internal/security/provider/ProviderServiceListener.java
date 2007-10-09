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

import org.eclipse.equinox.security.boot.EquinoxProvider;
import org.eclipse.equinox.security.provider.ProviderService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ProviderServiceListener implements ServiceTrackerCustomizer {
	private final BundleContext context;

	public ProviderServiceListener(BundleContext context) {
		this.context = context;
	}

	public Object addingService(ServiceReference reference) {
		ProviderService service = (ProviderService) context.getService(reference);
		((EquinoxProvider) service.getProvider()).registerService(service);
		return service;
	}

	public void modifiedService(ServiceReference reference, Object service) {
		// do nothing
	}

	public void removedService(ServiceReference reference, Object service) {
		ProviderService provider = (ProviderService) service;
		((EquinoxProvider) provider.getProvider()).unregisterService(provider);
	}
}
