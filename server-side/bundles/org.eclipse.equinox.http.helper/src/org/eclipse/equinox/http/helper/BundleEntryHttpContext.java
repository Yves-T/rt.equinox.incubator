/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation, Cognos Incorporated and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.equinox.http.helper;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public class BundleEntryHttpContext implements HttpContext {
	private Bundle bundle;
	private String bundlePath;

	public BundleEntryHttpContext(Bundle bundle) {
		this.bundle = bundle;
	}

	public BundleEntryHttpContext(Bundle b, String bundlePath) {
		this(b);
		if (bundlePath != null) {
			if (bundlePath.endsWith("/")) //$NON-NLS-1$
				bundlePath = bundlePath.substring(0, bundlePath.length() - 1);

			if (bundlePath.length() == 0)
				bundlePath = null;
		}
		this.bundlePath = bundlePath;
	}

	public String getMimeType(String arg0) {
		return null;
	}

	public boolean handleSecurity(HttpServletRequest arg0, HttpServletResponse arg1) throws IOException {
		return true;
	}

	public URL getResource(String resourceName) {
		if (bundlePath == null)
			return bundle.getEntry(resourceName);
		
		return bundle.getEntry(bundlePath + resourceName);
	}

	public Set getResourcePaths(String path) {
		Enumeration entryPaths = null;
		if (bundlePath == null)
			entryPaths = bundle.getEntryPaths(path);
		else
			entryPaths = bundle.getEntryPaths(bundlePath + path);
		
		if (entryPaths == null)
			return null;

		Set result = new HashSet();
		while (entryPaths.hasMoreElements()) {
			if (bundlePath == null)	
				result.add(entryPaths.nextElement());
			else
				result.add(((String)entryPaths.nextElement()).substring(bundlePath.length()));
		}
		return result;
	}
}