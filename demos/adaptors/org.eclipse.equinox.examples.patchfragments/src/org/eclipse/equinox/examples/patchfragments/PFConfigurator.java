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
package org.eclipse.equinox.examples.patchfragments;

import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;

/**
 * A hook configurator that enables patch fragments.  Patch fragments
 * are fragments that get their root content prepended to their host 
 * bundle's content. 
 * <p>
 * A fragment bundle specifies that it is a patch fragment by 
 * using the Equinox-BundleType header with the patch.fragment value
 * <pre>
 *   Equinox-BundleType: patch.fragment
 * </pre>
 * When a patch fragment is attached to a host bundle then its root content
 * is prepended to the host content.  This allows it to override files from 
 * the host bundle with patched content.
 * </p> 
 * <p>
 * This adaptor is for demonstration purposes.  It has not been productized.
 * </p>
 */
public class PFConfigurator implements HookConfigurator {

	public void addHooks(HookRegistry hookRegistry) {
		// this is where you add the needed hooks

		// an adaptor hook is needed to track PackageAdmin and add a BundleListener
		PFAdaptorHook adaptorHook = new PFAdaptorHook();
		hookRegistry.addAdaptorHook(adaptorHook);
		// a storage adaptor is needed to record the fragments which are patch fragments
		hookRegistry.addStorageHook(new PFStorageHook());
		// a bundle file wrapper is needed to intercept bundle entry requests to allow patched content
		hookRegistry.addBundleFileWrapperFactoryHook(new PFBundleWrapper(adaptorHook));
	}

}
