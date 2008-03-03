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

public class MCacheBundleFile extends BundleFile {

	private final BundleFile bundleFile;
	private final String cacheIndex;
	private final MCacheAdaptorHook cacheAdaptorHook;

	public MCacheBundleFile(BundleFile bundleFile, long bundleID, int hashCode, MCacheAdaptorHook cacheAdaptorHook) {
		this.bundleFile = bundleFile;
		this.cacheIndex = String.valueOf(bundleID) + '/' + String.valueOf(Math.abs(hashCode)) + '/';
		this.cacheAdaptorHook = cacheAdaptorHook;
	}

	public void close() throws IOException {
		bundleFile.close();
	}

	public boolean containsDir(String dir) {
		return cacheAdaptorHook.containsDir(dir, this);
	}

	public BundleEntry getEntry(String path) {
		return cacheAdaptorHook.getEntry(path, this);
	}

	public Enumeration getEntryPaths(String path) {
		return cacheAdaptorHook.getEntryPaths(path, this);
	}

	public File getFile(String path, boolean nativeCode) {
		return bundleFile.getFile(path, nativeCode);
	}

	public void open() throws IOException {
		bundleFile.open();
	}

	public File getBaseFile() {
		return bundleFile.getBaseFile();
	}

	BundleFile getBundleFile() {
		return bundleFile;
	}

	String getCacheIndex() {
		return cacheIndex;
	}

	public String toString() {
		return bundleFile.toString();
	}
}
