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

import org.eclipse.equinox.security.boot.IProviderService;
import org.eclipse.equinox.security.provider.ProviderService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class SecurityActivator implements BundleActivator {
	private static final String FILTER_STRING = "(|(objectclass=" + ProviderService.class.getName() + ")(objectclass=" + IProviderService.class.getName() + "))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private ServiceTracker providerTracker;

	public void start(BundleContext context) throws Exception {
		providerTracker = new ServiceTracker(context, context.createFilter(FILTER_STRING), new ProviderServiceListener(context));
		providerTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		providerTracker.close();
		providerTracker = null;
	}

}
