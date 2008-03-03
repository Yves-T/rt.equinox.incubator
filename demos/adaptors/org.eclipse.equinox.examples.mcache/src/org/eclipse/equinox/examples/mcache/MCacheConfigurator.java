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

/**
 * A hook configurator that enables a search miss cache.  A seach miss 
 * cache records when a resource search is done on bundle content but no 
 * match is found.  This allows the search to be short circuited the next time 
 * the same resource is searched.
 */
public class MCacheConfigurator implements HookConfigurator {

	public void addHooks(HookRegistry hookRegistry) {
		// the mcache adaptor hook is used to load the mcache at framework startup
		// and save the mcache at framework shutdown
		MCacheAdaptorHook w2CacheAdaptorHook = new MCacheAdaptorHook();
		hookRegistry.addAdaptorHook(w2CacheAdaptorHook);
		// a bundle file wrapper is needed to intercept bundle entry requests and check the mcache
		hookRegistry.addBundleFileWrapperFactoryHook(new MCacheBundleWrapper(w2CacheAdaptorHook));
	}

}
