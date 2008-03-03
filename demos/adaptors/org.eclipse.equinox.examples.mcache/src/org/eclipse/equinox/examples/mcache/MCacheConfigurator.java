/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.examples.mcache;

import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.framework.debug.FrameworkDebugOptions;

/**
 * A hook configurator that enables a search miss cache.  A seach miss 
 * cache records when a resource search is done on bundle content but no 
 * match is found.  This allows the search to be short circuited the next time 
 * the same resource is searched.
 */
public class MCacheConfigurator implements HookConfigurator {
	// the name of the folder to store the mcache
	final static String MCACHE_NAME = "org.eclipse.equinox.examples.mcache"; //$NON-NLS-1$
	// the name of the mcache file
	final static String MCACHE_FILE = "mcache.txt"; //$NON-NLS-1$

	static final boolean DEBUG;
	private static final String OPTION_DEBUG = MCacheConfigurator.MCACHE_NAME + "/debug"; //$NON-NLS-1$
	static {
		FrameworkDebugOptions options = FrameworkDebugOptions.getDefault();
		// may be null if debugging is not enabled
		if (options == null)
			DEBUG = false;
		else
			DEBUG = options.getBooleanOption(OPTION_DEBUG, false);
	}

	public void addHooks(HookRegistry hookRegistry) {
		// the mcache adaptor hook is used to load the mcache at framework startup
		// and save the mcache at framework shutdown
		MCacheAdaptorHook w2CacheAdaptorHook = new MCacheAdaptorHook();
		hookRegistry.addAdaptorHook(w2CacheAdaptorHook);
		// a bundle file wrapper is needed to intercept bundle entry requests and check the mcache
		hookRegistry.addBundleFileWrapperFactoryHook(new MCacheBundleWrapper(w2CacheAdaptorHook));
	}

}
