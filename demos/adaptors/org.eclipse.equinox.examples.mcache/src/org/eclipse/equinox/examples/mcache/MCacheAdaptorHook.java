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

import java.io.*;
import java.net.URLConnection;
import java.util.*;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.osgi.framework.BundleContext;

/**
 * Keeps track of the mcache.  Also loads the mcache at framework initialization and saves
 * the mcache at framework shutdown.
 */
public class MCacheAdaptorHook implements AdaptorHook {
	// the set of resource paths that are missing
	private final Set cache = new HashSet();
	// indicates that the mcach is dirty and should be persisted at shutdown
	private volatile boolean dirty = false;

	public void addProperties(Properties properties) {
		// nothing
	}

	public FrameworkLog createFrameworkLog() {
		// nothing
		return null;
	}

	public void frameworkStart(BundleContext context) {
		// nothing
	}

	/**
	 * Saves the mcach to a simple text file.
	 */
	public void frameworkStop(BundleContext context) {
		if (!dirty)
			return; // no need to save
		dirty = false;
		File cacheFile = getCacheFile();
		// simply write a list of paths misses
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(cacheFile)));
			synchronized (cache) {
				for (Iterator iPaths = cache.iterator(); iPaths.hasNext();)
					writer.println(iPaths.next());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	/**
	 * Gets the mcache file.  The mcache file is stored under the configuration location.
	 * @return the mcache file.
	 */
	private File getCacheFile() {
		File cacheDir = LocationManager.getConfigurationFile(MCacheConfigurator.MCACHE_NAME);
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		File cacheFile = new File(cacheDir, MCacheConfigurator.MCACHE_FILE);
		return cacheFile;
	}

	public void frameworkStopping(BundleContext context) {
		// nothing
	}

	public void handleRuntimeError(Throwable error) {
		// nothing
	}

	/**
	 * Initializes the mcache from disk.
	 */
	public void initialize(BaseAdaptor adaptor) {
		File cacheFile = getCacheFile();
		BufferedReader reader = null;
		try {
			if (cacheFile.exists()) {
				if (MCacheConfigurator.DEBUG)
					System.out.println("Loading mcache: " + cacheFile.getAbsolutePath()); //$NON-NLS-1$
				reader = new BufferedReader(new FileReader(cacheFile));
				synchronized (cache) {
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						cache.add(line);
						if (MCacheConfigurator.DEBUG)
							System.out.println(line);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}

	public URLConnection mapLocationToURLConnection(String location) {
		// do nothing;
		return null;
	}

	public boolean matchDNChain(String pattern, String[] dnChain) {
		// do nothing;
		return false;
	}

	/**
	 * Checks the mcache to see if the directory for the bundle file 
	 * is in the mcache.  If the path is in the MCache then false returned;
	 * otherwise the wrapped bundle file is searched.  If the wrapped 
	 * bundle file does not contain the path then the path is added to 
	 * the MCache and false is returned; otherwise true is returned.
	 * @param path the path to the directory
	 * @param mCacheBundleFile the bundle file that is being searched
	 * @return true if the bundle file contains the directory
	 */
	boolean containsDir(String path, MCacheBundleFile mCacheBundleFile) {
		String cachePath = checkCachePath(path, mCacheBundleFile);
		if (cachePath == null) // checkPath is null if there was a miss
			return false; // in the mcache; return false
		// now check the actual bundle file
		boolean result = mCacheBundleFile.getWrappedBundleFile().containsDir(path);
		if (!result) // did not find the path; add to mcache
			addToMCache(cachePath);
		return result;
	}

	/**
	 * Checks the mcache to see if the entry path for the bundle file 
	 * is in the mcache. If the path is in the MCache then null returned;
	 * otherwise the wrapped bundle file is searched.  If the wrapped 
	 * bundle file does not contain the path then the path is added to 
	 * the MCache and null is returned; otherwise entry from the 
	 * wrapped bundle file is returned.
	 * @param path the entry path
	 * @param mCacheBundleFile the bundle file that is being searched
	 * @return the entry for the path.
	 */
	BundleEntry getEntry(String path, MCacheBundleFile mCacheBundleFile) {
		String cachePath = checkCachePath(path, mCacheBundleFile);
		if (cachePath == null) // checkPath is null if there was a miss
			return null; // in the mcache; return null
		BundleEntry result = mCacheBundleFile.getWrappedBundleFile().getEntry(path);
		// now check the actual bundle file
		if (result == null) // did not find the path; add to mcache
			addToMCache(cachePath);
		return result;
	}

	/**
	 * Checks the mcache to see if the entry path for the bundle file 
	 * is in the mcache.  If the path is in the MCache then null returned;
	 * otherwise the wrapped bundle file is searched.  If the wrapped 
	 * bundle file does not contain the path then the path is added to 
	 * the MCache and null is returned; otherwise entry from the 
	 * wrapped bundle file is returned.
	 * @param path the entry path
	 * @param mCacheBundleFile the bundle file that is being searched
	 * @return the entry for the path.
	 */
	Enumeration getEntryPaths(String path, MCacheBundleFile mCacheBundleFile) {
		String cachePath = checkCachePath(path, mCacheBundleFile);
		if (cachePath == null) // checkPath is null if there was a miss
			return null; // in the mcache; return null
		// now check the actual bundle file
		Enumeration result = mCacheBundleFile.getWrappedBundleFile().getEntryPaths(path);
		if (result == null) // did not find the path; add to mcache
			addToMCache(cachePath);
		return result;
	}

	/**
	 * Checks the mcache for path in the specified bundle file.  A value 
	 * of <code>null</code> is returned if the path exists in the cache for
	 * the specified bundle file; otherwise a cache path is returned.  The
	 * cache path is <code>{@link MCacheBundleFile#getCacheIndex()} + path</code>
	 * @param path the entry path
	 * @param cacheBundleFile the bundle file to check
	 * @return null if the path is in the cache; otherwise the cache path is returned
	 */
	private String checkCachePath(String path, MCacheBundleFile cacheBundleFile) {
		// check for null and empty paths
		if (path == null || path.length() == 0)
			return ""; //$NON-NLS-1$
		// check for leading '/' and remove
		if (path.charAt(0) == '/')
			path = path.substring(1);
		// use a path of cacheIndex + path
		path = cacheBundleFile.getCacheIndex() + path;
		synchronized (cache) {
			if (MCacheConfigurator.DEBUG && cache.contains(path))
				System.out.println("Found in mcache: " + path); //$NON-NLS-1$
			// if the path is in the cache return null; otherwise return the cache path
			return cache.contains(path) ? null : path;
		}
	}

	/**
	 * Adds the cache path to the mcache.
	 * @param path the cache path
	 */
	private void addToMCache(String path) {
		synchronized (cache) {
			if (MCacheConfigurator.DEBUG)
				System.out.println("Add to mcache: " + path); //$NON-NLS-1$
			cache.add(path);
			dirty = true;
		}
	}
}
