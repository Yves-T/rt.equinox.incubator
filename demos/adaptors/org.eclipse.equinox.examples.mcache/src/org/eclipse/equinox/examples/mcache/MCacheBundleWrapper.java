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

import java.io.File;
import java.io.IOException;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.hooks.BundleFileWrapperFactoryHook;

/**
 * Wraps bundle files so that they can be patched with content from
 * patch fragments.
 */
public class MCacheBundleWrapper implements BundleFileWrapperFactoryHook {

	private final MCacheAdaptorHook cacheAdaptorHook;

	public MCacheBundleWrapper(MCacheAdaptorHook cacheAdaptorHook) {
		this.cacheAdaptorHook = cacheAdaptorHook;
	}

	public BundleFile wrapBundleFile(BundleFile bundleFile, Object content, BaseData data, boolean base) throws IOException {
		if (data.getBundleID() == 0)
			// it is usually a bad idea to modify the behavior of the system.bundle file.
			return null;
		if (!(content instanceof File))
			return null; // we only wrapper File content
		// The canonical file path of the content is used to create a unique index into the MCache
		String path = ((File) content).getCanonicalPath();
		// Create an MCacheBundleFile, the content path and bundle id are used to create a unique index
		MCacheBundleFile result = new MCacheBundleFile(bundleFile, data.getBundleID(), path.hashCode(), cacheAdaptorHook);
		return result;
	}

}
