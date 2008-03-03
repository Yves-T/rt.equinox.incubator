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

public class MCacheBundleWrapper implements BundleFileWrapperFactoryHook {

	private final MCacheAdaptorHook cacheAdaptorHook;

	public MCacheBundleWrapper(MCacheAdaptorHook cacheAdaptorHook) {
		this.cacheAdaptorHook = cacheAdaptorHook;
	}

	public BundleFile wrapBundleFile(BundleFile bundleFile, Object content, BaseData data, boolean base) throws IOException {
		if (data.getBundleID() == 0)
			return null;
		String path = ((File) content).getCanonicalPath();
		MCacheBundleFile result = new MCacheBundleFile(bundleFile, data.getBundleID(), path.hashCode(), cacheAdaptorHook);
		return result;
	}

}
