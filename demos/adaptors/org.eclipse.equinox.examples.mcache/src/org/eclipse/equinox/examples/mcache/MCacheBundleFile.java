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
import java.util.Enumeration;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;

/**
 * A bundle file that wraps the content of another bundle file.
 * If a cache miss is detected for a path then it is recorded.  
 * If the same path is searched again then the search is short
 * circuited and the wrapped BundleFile is not searched again for 
 * that path.
 */
public class MCacheBundleFile extends BundleFile {
	/**
	 * The wrapped bundle file that is being patched
	 */
	private final BundleFile wrapped;
	/**
	 * A unique cache index for the wrapped bundle file.  This is based
	 * on the bundle ID and the hash code of the bundle file canonical path.
	 */
	private final String cacheIndex;
	/**
	 * The MCache adaptor hook
	 */
	private final MCacheAdaptorHook cacheAdaptorHook;

	public MCacheBundleFile(BundleFile bundleFile, long bundleID, int hashCode, MCacheAdaptorHook cacheAdaptorHook) {
		// use the base file from the wrapped bundle file
		super(bundleFile.getBaseFile());
		this.wrapped = bundleFile;
		this.cacheIndex = String.valueOf(bundleID) + '/' + String.valueOf(Math.abs(hashCode)) + '/';
		this.cacheAdaptorHook = cacheAdaptorHook;
	}

	public void close() throws IOException {
		wrapped.close();
	}

	/**
	 * Checks the MCache for the directory.
	 */
	public boolean containsDir(String dir) {
		return cacheAdaptorHook.containsDir(dir, this);
	}

	/**
	 * Checks the MCache for the path.
	 */
	public BundleEntry getEntry(String path) {
		return cacheAdaptorHook.getEntry(path, this);
	}

	/**
	 * Checks the MCache for the path.
	 */
	public Enumeration getEntryPaths(String path) {
		return cacheAdaptorHook.getEntryPaths(path, this);
	}

	public File getFile(String path, boolean nativeCode) {
		return wrapped.getFile(path, nativeCode);
	}

	public void open() throws IOException {
		wrapped.open();
	}

	BundleFile getBundleFile() {
		return wrapped;
	}

	String getCacheIndex() {
		return cacheIndex;
	}

	public String toString() {
		return wrapped.toString();
	}
}
